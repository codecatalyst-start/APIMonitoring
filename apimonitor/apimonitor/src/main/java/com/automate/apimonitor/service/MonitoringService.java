package com.automate.apimonitor.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.automate.apimonitor.model.ErrorPayload;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class MonitoringService {
	
	 private final RestTemplate restTemplate = new RestTemplate();


	    @Value("${monitoring.webhook-url}")
	    private String webhookUrl;

	    public void send(ErrorPayload payload) {
	        try {
	            restTemplate.postForEntity(
	                    webhookUrl,
	                    payload,
	                    Void.class
	            );
	        } catch (Exception ex) {
	            System.err.println("Failed to notify n8n: " + ex.getMessage());
	        }
	    }

}
