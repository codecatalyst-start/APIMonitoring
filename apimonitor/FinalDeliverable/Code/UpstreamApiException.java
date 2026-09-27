package com.example.faillogger.exception;

/**
 * Thrown when a call to an upstream (third-party / microservice) API
 * completes but returns a non-2xx HTTP status. Carries the real status
 * code and response body so it can be reported accurately.
 */
public class UpstreamApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public UpstreamApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
