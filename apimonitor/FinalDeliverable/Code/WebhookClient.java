package com.example.faillogger.client;

import com.example.faillogger.model.ApiFailureEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends ApiFailureEvent payloads to the n8n webhook, which in turn
 * enriches them with an AI suggestion and stores them in MongoDB.
 */
public class WebhookClient {

    private final String webhookUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WebhookClient(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Serializes the event to JSON and POSTs it to the n8n webhook.
     */
    public HttpResponse<String> send(ApiFailureEvent event) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(event);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(3))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
