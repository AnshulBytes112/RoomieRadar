package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Service.BookingService;
import com.anshul.RoomieRadarBackend.Service.UserService;
import com.anshul.RoomieRadarBackend.dto.BookingDTO;
import com.anshul.RoomieRadarBackend.dto.BookingRequest;
import com.anshul.RoomieRadarBackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    public UserService userService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest bookingRequest, Authentication authentication) {
        try {
            User bookedBy = userService.findByEmail(authentication.getName());
            BookingDTO createdBooking = bookingService.createBooking(bookingRequest, bookingRequest.getRoomId(),
                    bookedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating booking: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getBookings(Authentication authentication) {
        try {
            return ResponseEntity.ok(bookingService.getBookingsByUser(authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving bookings: " + e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteBooking(@RequestParam Long bookingId, Authentication authentication) {
        if (bookingId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking ID is required");
        }
        try {
            bookingService.cancelBooking(bookingId, authentication.getName());
            return ResponseEntity.ok("Booking deleted successfully. Room owner has been notified.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting booking: " + e.getMessage());
        }
    }
}
