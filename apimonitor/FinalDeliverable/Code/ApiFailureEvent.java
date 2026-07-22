package com.example.faillogger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mirrors the api_failure_log / MongoDB "api_failure_log" collection schema:
 *
 *   service_name, endpoint, method, status, exception_name,
 *   message, stacktrace, request_body, created_at
 *
 * "ai_suggestion" and "id/_id" are intentionally omitted here — they are
 * generated server-side by the n8n workflow / MongoDB, not by the client.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiFailureEvent {

    @JsonProperty("serviceId")
    private int serviceId;

    @JsonProperty("statusCode")
    private int statusCode;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("endpoint")
    private String endpoint;

    @JsonProperty("method")
    private String method;

    /** HTTP status returned by the upstream call. 0 if no response was ever received (e.g. connection/DNS/timeout failure). */
    @JsonProperty("status")
    private int status;

    @JsonProperty("exception_name")
    private String exceptionName;

    @JsonProperty("message")
    private String message;

    @JsonProperty("stacktrace")
    private String stacktrace;

    @JsonProperty("request_body")
    private String requestBody;

    @JsonProperty("created_at")
    private String createdAt;

    // Extra correlation field — not part of the Mongo schema's required fields,
    // but harmless since the collection validator does not set additionalProperties:false.
    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("exception_type")
    private String exceptionType;

    @JsonProperty("error_message")
    private String errorMessage;

     @JsonProperty("stackTrace")
    private String stackTrace;

       @JsonProperty("aiRootCause")
    private String aiRootCause;

    
       @JsonProperty("aiFixRecommendation")
    private String aiFixRecommendation;

     @JsonProperty("aiCodeExample")
    private String aiCodeExample;

    public ApiFailureEvent() {
    }

    private ApiFailureEvent(Builder b) {
        this.serviceId = b.serviceId;
        this.statusCode = b.statusCode;
        this.serviceName = b.serviceName;
        this.endpoint = b.endpoint;
        this.method = b.method;
        this.status = b.status;
        this.exceptionName = b.exceptionName;
        this.message = b.message;
        this.stacktrace = b.stacktrace;
        this.requestBody = b.requestBody;
        this.createdAt = b.createdAt;
        this.requestId = b.requestId;
        this.severity = b.severity;
        this.exceptionType = b.exceptionType;
        this.errorMessage = b.errorMessage;
        this.stackTrace = b.stackTrace;
        this.aiRootCause = b.aiRootCause;
        this.aiFixRecommendation = b.aiFixRecommendation;
        this.aiCodeExample = b.aiCodeExample;
    }

    public static Builder builder() {
        return new Builder();
    }

    // --- getters (needed for Jackson + general use) ---
    public int getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public String getEndpoint() { return endpoint; }
    public String getMethod() { return method; }
    public int getStatus() { return status; }
     public int getStatusCode() { return statusCode; }
    public String getExceptionName() { return exceptionName; }
    public String getMessage() { return message; }
    public String getStacktrace() { return stacktrace; }
    public String getRequestBody() { return requestBody; }
    public String getCreatedAt() { return createdAt; }
    public String getRequestId() { return requestId; }
    public String getSeverity() { return severity; }
    public String getExceptionType() { return exceptionType; }
    public String getErrorMessage() { return errorMessage;}
    public String getStackTrace() { return stackTrace; }
    public String getAiRootCause() { return aiRootCause; }
    public String getAiFixRecommendation() { return aiFixRecommendation; }
    public String getAiCodeExample() { return aiCodeExample; }

    public static class Builder {
        public String stackTrace;
        private int serviceId;
        private String serviceName;
        private String endpoint;
        private String method;
        private int status;
        private int statusCode;
        private String exceptionName;
        private String message;
        private String stacktrace;
        private String requestBody;
        private String createdAt;
        private String requestId;
        private String severity;
        private String exceptionType;
        private String errorMessage;
        private String aiRootCause;
        private String aiFixRecommendation;
        private String aiCodeExample;


        public Builder serviceId(int v) { this.serviceId = v; return this; }
        public Builder serviceName(String v) { this.serviceName = v; return this; }
        public Builder endpoint(String v) { this.endpoint = v; return this; }
        public Builder method(String v) { this.method = v; return this; }
        public Builder status(int v) { this.status = v; return this; }
        public Builder statusCode(int v) { this.statusCode = v; return this; }
        public Builder exceptionName(String v) { this.exceptionName = v; return this; }
        public Builder message(String v) { this.message = v; return this; }
        public Builder stacktrace(String v) { this.stacktrace = v; return this; }
        public Builder requestBody(String v) { this.requestBody = v; return this; }
        public Builder createdAt(String v) { this.createdAt = v; return this; }
        public Builder requestId(String v) { this.requestId = v; return this; }
        public Builder severity(String v) { this.severity = v; return this; }
        public Builder exceptionType(String v) { this.exceptionType = v; return this; }
        public Builder errorMessage(String v) { this.errorMessage = v; return this; }
        public Builder stackTrace(String v) { this.stackTrace = v; return this; }
        public Builder aiRootCause(String v) { this.aiRootCause = v; return this; }
        public Builder aiFixRecommendation(String v) { this.aiFixRecommendation = v; return this; }
        public Builder aiCodeExample(String v) { this.aiCodeExample = v; return this; }

        public ApiFailureEvent build() { return new ApiFailureEvent(this); }
    }
}
