package org.agri.agritrade.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.service.SmsServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SmsService implements SmsServicePort {

    @Value("${textbee.api-key}")
    private String apiKey;

    @Value("${textbee.device-id}")
    private String deviceId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Async
    public void sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("Cannot send SMS: phone number is empty");
            return;
        }

        try {
            String url = "https://api.textbee.dev/api/v1/gateway/devices/" + deviceId + "/sendSMS";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            Map<String, Object> body = Map.of(
                    "recipients", List.of(phoneNumber),
                    "message", message
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent successfully to {}", phoneNumber);
            } else {
                log.error("Failed to send SMS to {}. Status: {}", phoneNumber, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }
}
