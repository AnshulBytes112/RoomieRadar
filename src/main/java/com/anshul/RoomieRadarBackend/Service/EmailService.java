package com.anshul.RoomieRadarBackend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
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
