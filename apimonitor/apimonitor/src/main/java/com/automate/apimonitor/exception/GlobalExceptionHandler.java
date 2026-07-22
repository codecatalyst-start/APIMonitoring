package com.automate.apimonitor.exception;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.automate.apimonitor.model.ErrorPayload;
import com.automate.apimonitor.service.MonitoringService;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@Autowired
	private MonitoringService monitoringService;
	
	 @ExceptionHandler(Exception.class)
	    public ResponseEntity<ErrorPayload> handleException(
	            Exception ex,
	            HttpServletRequest request) {

		 ErrorPayload payload = ErrorPayload.builder()
	                .service("Order-Service")
	                .endpoint(request.getRequestURI())
	                .method(request.getMethod())
	                .status(500)
	                .exception(ex.getClass().getSimpleName())
	                .message(ex.getMessage())
	                .stackTrace(Arrays.toString(ex.getStackTrace()))
	                .timestamp(LocalDateTime.now().toString())
	                .build();

	        monitoringService.send(payload);

	        return ResponseEntity.status(500).body(payload);
	    }

}
