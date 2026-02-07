package com.anshul.RoomieRadarBackend.Service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BookingMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public BookingMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendBookingEmails(
            String userName,
            String userEmail,
            String userPhone,
            String ownerEmail,
            String roomTitle,
            java.time.LocalDate checkInDate,
            String message) {
        sendMailToUser(userName, userEmail, roomTitle, checkInDate);
        sendMailToOwner(userName, userEmail, userPhone, ownerEmail, roomTitle, checkInDate, message);
    }

    private void sendMailToUser(
            String userName,
            String userEmail,
            String roomTitle,
            LocalDate moveInDate) {
        String subject = "Booking Request Received – " + roomTitle;

        String body = """
                Hi %s,

                Your booking request for the room "%s" has been successfully sent.

                📅 Preferred Move-in Date: %s

                The owner will contact you shortly.
                You can track your request in the RoomieRadar dashboard.

                This is only a booking request. No payment has been made.

                Regards,
                RoomieRadar Team
                """.formatted(userName, roomTitle, moveInDate);

        sendEmail(userEmail, subject, body);
    }

    private void sendMailToOwner(
            String userName,
            String userEmail,
            String userPhone,
            String ownerEmail,
            String roomTitle,
            LocalDate moveInDate,
            String message) {
        String subject = "New Booking Request – " + roomTitle;

        String body = """
                You have received a new booking request.

                👤 Name: %s
                📧 Email: %s
                📞 Phone: %s
                🏠 Room: %s
                📅 Preferred Move-in Date: %s

                💬 Message from user:
                "%s"

                Please log in to RoomieRadar to respond.

                – RoomieRadar
                """.formatted(
                userName,
                userEmail,
                userPhone,
                roomTitle,
                moveInDate,
                message == null ? "No message provided" : message);

        sendEmail(ownerEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (fromEmail == null || fromEmail.isEmpty()) {
            System.err.println("CRITICAL: spring.mail.username is not configured! Cannot send email.");
            return;
        }
        System.out.println("DEBUG: Sending email from: " + fromEmail + " to: " + to);
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);

            mailSender.send(mail);
            System.out.println("DEBUG: Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to send email to " + to + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Async
    public void sendBookingCancellationEmails(String userName, String userEmail, String roomTitle,
            java.time.LocalDate checkInDate) {
        String subject = "Booking Cancellation – " + roomTitle;

        String body = """
                Hi %s,

                Your booking for the room "%s" scheduled for %s has been successfully cancelled.

                If you have any questions, please contact our support team.

                Regards,
                RoomieRadar Team
                """.formatted(userName, roomTitle, checkInDate);

        sendEmail(userEmail, subject, body);

    }

    public void sendBookingCancellationNotificationToOwner(String ownerEmail, String ownerName, String roomTitle,
            String userName, LocalDate checkInDate) {
        String subject = "Booking Cancelled – " + roomTitle;

        String body = """
                Hi %s,

                A booking for your room "%s" has been cancelled by %s.

                The booking was scheduled for: %s

                The room is now available for other bookings.

                Regards,
                RoomieRadar Team
                """.formatted(ownerName, roomTitle, userName, checkInDate);

        sendEmail(ownerEmail, subject, body);
    }
}
