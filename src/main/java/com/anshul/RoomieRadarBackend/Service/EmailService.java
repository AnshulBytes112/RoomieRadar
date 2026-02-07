package com.anshul.RoomieRadarBackend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {
    private final RestTemplate restTemplate;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendEmail(String to, String subject, String content) {
        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + resendApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", "RoomieRadar <" + fromEmail + ">");
        body.put("to", to);
        body.put("subject", subject);
        body.put("text", content); // Using plain text for now, can use "html" for formatted emails

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email via Resend to {}: {}", to, e.getMessage());
            // In a real app, you might want to retry or throw a custom exception
        }
    }

    public void sendOtpEmail(String to, String otp) {
        String subject = "Verify your RoomieRadar Account";
        String body = "Hello,\n\nYour OTP for account verification is: " + otp
                + "\n\nThis code will expire in 10 minutes.\n\nRegards,\nRoomieRadar Team";

        sendEmail(to, subject, body);
    }

    public void sendPasswordResetOtpEmail(String to, String otp) {
        String subject = "Reset your RoomieRadar Password";
        String body = "Hello,\n\nWe received a request to reset your password. Use the security code below to proceed:\n\n"
                + otp + "\n\nIf you did not request this, please ignore this email.\n\nRegards,\nRoomieRadar Team";

        sendEmail(to, subject, body);
    }
}
