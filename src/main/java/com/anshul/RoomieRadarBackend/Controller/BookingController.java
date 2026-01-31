package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Service.BookingMailService;
import com.anshul.RoomieRadarBackend.Service.BookingService;
import com.anshul.RoomieRadarBackend.Service.UserService;
import com.anshul.RoomieRadarBackend.dto.BookingRequest;
import com.anshul.RoomieRadarBackend.entity.Booking;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingMailService bookingMailService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    public UserService userService;
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest bookingRequest,Authentication authentication) {
        try{
            System.out.println("DEBUG: Creating booking with data: " + bookingRequest);
            
            Long RoomId= bookingRequest.getRoomId();
            User bookedby= userService.findByUsername(authentication.getName());
            System.out.println("DEBUG: Found user: " + bookedby.getName() + " (ID: " + bookedby.getId() + ")");
            
            User roomcreator = bookingService.getRoomCreator(RoomId);
            System.out.println("DEBUG: Room creator: " + roomcreator.getName() + " (Email: " + roomcreator.getEmail() + ")");
            
            // Send emails only if user has opted in for email confirmation
            if (bookingRequest.getSendEmailConfirmation() != null && bookingRequest.getSendEmailConfirmation()) {
                System.out.println("DEBUG: Sending email confirmation...");
                try {
                    bookingMailService.sendBookingEmails(
                            bookedby.getName(),
                            bookedby.getEmail(),
                            bookedby.getPhone(),
                            roomcreator.getEmail(),
                            bookingRequest.getRoomId().toString(),
                            bookingRequest.getCheckInDate(),
                            bookingRequest.getMessage()
                    );
                    System.out.println("DEBUG: Emails sent successfully");
                } catch (Exception emailError) {
                    System.err.println("WARNING: Failed to send emails but proceeding with booking: " + emailError.getMessage());
                    // Don't re-throw email errors - booking should still succeed
                }
            } else {
                System.out.println("DEBUG: Email confirmation disabled");
            }
            
            System.out.println("DEBUG: Creating booking in database...");
            Booking createdBooking = bookingService.createBooking(bookingRequest, RoomId, bookedby);
            System.out.println("DEBUG: Booking created successfully with ID: " + createdBooking.getId());
            
            return ResponseEntity.status(201).body(createdBooking);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create booking: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace
            return ResponseEntity.status(500).body("Error creating booking: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getBookings(Authentication authentication) {
        try {
            List<Booking> bookings = bookingService.getBookingsByUser(authentication.getName());
            return ResponseEntity.ok(bookings);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving bookings: " + e.getMessage());
        }
       
    }

    @DeleteMapping
    public ResponseEntity<?> deleteBooking(@RequestParam Long bookingId, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            Booking booking = bookingRepository.findById(bookingId).orElse(null);

            if (booking == null || !booking.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(404).body("Booking not found or does not belong to user");
            }
            
            // Capture booking details before deleting
            User roomOwner = booking.getRoom().getPostedBy();
            String roomTitle = booking.getRoom().getTitle();
            LocalDate checkInDate = booking.getCheckInDate();
            String userName = user.getName();
            String userEmail = user.getEmail();
            String ownerEmail = roomOwner.getEmail();
            String ownerName = roomOwner.getName();
            
            bookingService.deleteBooking(bookingId);
            
            // Send cancellation email to the user who cancelled
            bookingMailService.sendBookingCancellationEmails(
                    userName,
                    userEmail,
                    roomTitle,
                    checkInDate
            );
            
            // Send notification email to the room owner
            bookingMailService.sendBookingCancellationNotificationToOwner(
                    ownerEmail,
                    ownerName,
                    roomTitle,
                    userName,
                    checkInDate
            );
            
            return ResponseEntity.ok("Booking deleted successfully. Room owner has been notified.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting booking: " + e.getMessage());
        }
    }
}
