package com.anshul.RoomieRadarBackend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String otp) {
        String subject = "Verify your RoomieRadar Account";
        String body = "Hello,\n\nYour OTP for account verification is: " + otp
                + "\n\nThis code will expire in 10 minutes.\n\nRegards,\nRoomieRadar Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendPasswordResetOtpEmail(String to, String otp) {
        String subject = "Reset your RoomieRadar Password";
        String body = "Hello,\n\nWe received a request to reset your password. Use the security code below to proceed:\n\n"
                + otp + "\n\nIf you did not request this, please ignore this email.\n\nRegards,\nRoomieRadar Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
