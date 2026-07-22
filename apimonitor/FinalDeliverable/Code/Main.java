package com.example.faillogger;

import com.example.faillogger.client.WebhookClient;
import com.example.faillogger.exception.UpstreamApiException;
import com.example.faillogger.model.ApiFailureEvent;
import com.example.faillogger.service.FreeApiClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Demonstrates calling real, free, publicly reachable APIs, letting them
 * fail for real reasons (404, 500, connection timeout, DNS failure), and
 * forwarding the *actual* captured exception as JSON to the n8n webhook
 * -> database -> AI-fix pipeline described in the API Failure Log guide.
 *
 * No exceptions are faked or hand-written — every payload sent here is
 * built from a genuine Throwable caught while talking to a real endpoint.
 *
 * IMPORTANT FIX vs the previous version:
 * Both catch branches now populate the SAME set of fields
 * (statusCode, severity, exceptionType, errorMessage, stackTrace, serviceId, ...).
 * Previously the network-failure branch used a different, legacy field set
 * (exceptionName / message / stacktrace / status) than the HTTP-failure branch
 * (exceptionType / errorMessage / stackTrace / statusCode). Since the n8n
 * workflow and database schema only read the second set, every network-level
 * failure (timeout, DNS failure) was silently forwarded with null
 * exception_type / error_message / stack_trace / severity.
 */
public class Main {

    // Replace with your own n8n webhook URL, or set env var N8N_WEBHOOK_URL
    private static final String DEFAULT_WEBHOOK_URL =
            "https://amarjapd.app.n8n.cloud/webhook/api-failure";

    public static void main(String[] args) throws Exception {
        String webhookUrl = System.getenv().getOrDefault("N8N_WEBHOOK_URL", DEFAULT_WEBHOOK_URL);

        WebhookClient webhookClient = new WebhookClient(webhookUrl);
        FreeApiClient apiClient = new FreeApiClient(Duration.ofSeconds(3));

        System.out.println("Forwarding real API failures to: " + webhookUrl);
        System.out.println("----------------------------------------------------");

        // 1) A real 404 from a free public API (id out of range)
        callAndReport(
                webhookClient, apiClient,
                "order-service",
                "https://jsonplaceholder.typicode.com/posts/999999",
                "GET", null
        );
 
        // 2) A real 500 from a free API built for testing HTTP status codes
        callAndReport(
                webhookClient, apiClient,
                "checkout-service",
                "https://httpstat.us/500",
                "POST", "{\"orderId\":1,\"cartId\":\"c-9911\"}"
        );

        // 3) A real connection timeout — 192.0.2.0/24 is IANA's reserved
        // "TEST-NET-1" documentation range (RFC 5737): it is guaranteed to
        // never be routed or answered, so the connection reliably times out
        // instead of behaving unpredictably like a private 10.x address might
        // on a real corporate network.
        callAndReport(
                webhookClient, apiClient,
                "payment-gateway-service",
                "https://192.0.2.1/api/v1/charge",
                "POST", "{\"amount\":100,\"currency\":\"USD\"}"
        );

        // 4) A real DNS resolution failure (domain does not exist)
        callAndReport(
                webhookClient, apiClient,
                "shipping-service",
                "https://this-domain-definitely-does-not-exist-abcxyz123456.com/api/v1/rates",
                "GET", null
        );

        System.out.println("----------------------------------------------------");
        System.out.println("Done. Check your n8n executions / DB collection.");
    }

    /**
     * Calls a real endpoint. If it fails, builds an ApiFailureEvent from the
     * *actual* caught exception and forwards it to the webhook.
     */
    private static void callAndReport(WebhookClient webhookClient,
                                       FreeApiClient apiClient,
                                       String serviceName,
                                       String url,
                                       String method,
                                       String requestBody) {
        String endpoint = extractPath(url);
        System.out.println("Calling " + serviceName + " -> " + method + " " + url);

        try {
            String result = "GET".equalsIgnoreCase(method)
                    ? apiClient.get(url, Duration.ofSeconds(4))
                    : apiClient.post(url, requestBody, Duration.ofSeconds(4));
            System.out.println("  OK (no failure to report): " + truncate(result, 120));

        } catch (UpstreamApiException e) {
            // Real non-2xx HTTP response from the upstream service.
            ApiFailureEvent event = buildEvent(
                    serviceName, endpoint, method, requestBody,
                    e.getStatusCode(),                 // real HTTP status code
                    severityForStatus(e.getStatusCode()),
                    e.getClass().getName(),
                    e.getMessage(),
                    stackTraceOf(e)
            );
            send(webhookClient, event);

        } catch (Exception e) {
            // Real network-level failure: connect timeout, DNS failure, I/O error, etc.
            // No HTTP response was ever received, so statusCode = 0.
            ApiFailureEvent event = buildEvent(
                    serviceName, endpoint, method, requestBody,
                    0,
                    "CRITICAL",
                    e.getClass().getName(),
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(),
                    stackTraceOf(e)
            );
            send(webhookClient, event);
        }
    }

    /**
     * Builds a fully-populated ApiFailureEvent using ONE consistent set of
     * fields regardless of failure type, so no downstream consumer
     * (n8n workflow, database column, Grafana panel) ever receives a
     * silent null for serviceId, statusCode, severity, exceptionType,
     * errorMessage, or stackTrace.
     *
     * AI fields (aiRootCause / aiFixRecommendation / aiCodeExample) are
     * intentionally left unset here — they are generated server-side by the
     * n8n AI agent, never faked by the client.
     */
    private static ApiFailureEvent buildEvent(String serviceName,
                                               String endpoint,
                                               String method,
                                               String requestBody,
                                               int statusCode,
                                               String severity,
                                               String exceptionType,
                                               String errorMessage,
                                               String stackTrace) {
        return ApiFailureEvent.builder()
                .serviceId(generateServiceId())
                .serviceName(serviceName)
                .endpoint(endpoint)
                .method(method)
                .statusCode(statusCode)
                .status(statusCode)              // keep legacy "status" mirror in sync
                .severity(severity)
                .exceptionType(exceptionType)
                .errorMessage(errorMessage)
                .stackTrace(stackTrace)
                .requestBody(requestBody)
                .requestId(UUID.randomUUID().toString())
                .createdAt(Instant.now().toString())
                .build();
    }

    private static void send(WebhookClient webhookClient, ApiFailureEvent event) {
        try {
            HttpResponse<String> response = webhookClient.send(event);
            System.out.println("  -> Forwarded failure (" + event.getExceptionType()
                    + ", statusCode=" + event.getStatusCode() + ")"
                    + " | webhook HTTP " + response.statusCode());
        } catch (Exception forwardEx) {
            // If even the webhook call fails, don't crash the app — just log it.
            System.err.println("  -> Failed to forward event to webhook: " + forwardEx);
        }
    }

    /**
     * Generates a positive, non-zero serviceId.
     * Math.abs(UUID.randomUUID().hashCode()) is NOT safe on its own: for the
     * one hashCode value equal to Integer.MIN_VALUE, Math.abs() overflows and
     * returns MIN_VALUE unchanged (still negative). Using ThreadLocalRandom
     * directly avoids that edge case and guarantees a value in [1, 999999].
     */
    private static int generateServiceId() {
        return ThreadLocalRandom.current().nextInt(1, 1_000_000);
    }

    private static String severityForStatus(int statusCode) {
        if (statusCode >= 500) return "CRITICAL";
        if (statusCode >= 400) return "HIGH";
        return "MEDIUM";
    }

    private static String stackTraceOf(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String extractPath(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            String path = uri.getPath();
            return (path == null || path.isEmpty()) ? "/" : path;
        } catch (Exception e) {
            return url;
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}