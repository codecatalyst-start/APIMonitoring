package com.example.faillogger.service;

import com.example.faillogger.exception.UpstreamApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin wrapper around java.net.http.HttpClient used to call real, free,
 * publicly reachable APIs/microservices. It does NOT swallow exceptions —
 * network failures (unreachable host, DNS failure, timeout) propagate as-is
 * so the caller can capture the *real* exception type/message/stacktrace,
 * and non-2xx HTTP responses are surfaced as UpstreamApiException.
 */
public class FreeApiClient {

    private final HttpClient httpClient;

    public FreeApiClient() {
        this(Duration.ofSeconds(3));
    }

    public FreeApiClient(Duration connectTimeout) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
    }

    /**
     * Performs a GET request.
     *
     * @throws UpstreamApiException if the server responds with a non-2xx status
     * @throws IOException          on connection failure, DNS failure, etc.
     * @throws InterruptedException if the calling thread is interrupted
     */
    public String get(String url, Duration requestTimeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(requestTimeout)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return handle(response);
    }

    /**
     * Performs a POST request with a JSON body.
     *
     * @throws UpstreamApiException if the server responds with a non-2xx status
     * @throws IOException          on connection failure, DNS failure, etc.
     * @throws InterruptedException if the calling thread is interrupted
     */
    public String post(String url, String jsonBody, Duration requestTimeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(requestTimeout)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return handle(response);
    }

    private String handle(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new UpstreamApiException(
                    "Upstream call failed with HTTP " + status,
                    status,
                    response.body()
            );
        }
        return response.body();
    }
}
