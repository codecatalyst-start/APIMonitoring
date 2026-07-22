package com.automate.apimonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorPayload {

    private String service;

    private String endpoint;

    private String method;

    private Integer status;

    private String exception;

    private String message;

    private String stackTrace;

    private String timestamp;
}
