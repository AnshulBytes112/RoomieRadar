package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.Mapper.BookingMapper;
import com.anshul.RoomieRadarBackend.dto.BookingDTO;
import com.anshul.RoomieRadarBackend.dto.BookingRequest;
import com.anshul.RoomieRadarBackend.entity.Booking;
import com.anshul.RoomieRadarBackend.entity.Room;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.BookingRepository;
import com.anshul.RoomieRadarBackend.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {
    @Autowired
    private BookingMailService bookingMailService;

    @Autowired
    UserService userService;
    @Autowired
    RoomRepository roomRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Transactional
    public BookingDTO createBooking(BookingRequest bookingRequest, Long roomId, User user) {
        if (roomId == null)
            throw new IllegalArgumentException("RoomId cannot be null");

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found"));

            Booking booking = new Booking();
            booking.setRoom(room);
            booking.setUser(user);
            booking.setCheckInDate(bookingRequest.getCheckInDate());
            booking.setCheckInDateField(bookingRequest.getCheckInDate());
            booking.setPhone(bookingRequest.getPhone());
            booking.setMessage(bookingRequest.getMessage());
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setCreatedAt(LocalDateTime.now());

            Booking savedBooking = bookingRepository.save(booking);

            // Handle Email Notification
            if (Boolean.TRUE.equals(bookingRequest.getSendEmailConfirmation())) {
                try {
                    User roomCreator = room.getPostedBy();
                    if (roomCreator != null && roomCreator.getEmail() != null) {
                        bookingMailService.sendBookingEmails(
                                user.getName(),
                                user.getEmail(),
                                user.getPhone(),
                                roomCreator.getEmail(),
                                room.getTitle(),
                                bookingRequest.getCheckInDate(),
                                bookingRequest.getMessage());
                    } else {
                        System.err.println(
                                "WARNING: Cannot send booking email to owner - owner details are missing for room: "
                                        + room.getTitle());
                    }
                } catch (Exception emailError) {
                    System.err.println("WARNING: Failed to send booking emails: " + emailError.getMessage());
                    emailError.printStackTrace();
                }
            }

            return BookingMapper.toDto(savedBooking);
        } catch (Exception e) {
            System.err.println("ERROR in BookingService.createBooking: " + e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public User getRoomCreator(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        return room.getPostedBy();
    }

    @Transactional(readOnly = true)
    public BookingDTO getBooking(String email) {
        User user = userService.findByEmail(email);
        Optional<Booking> booking = bookingRepository.findByUserId(user.getId());
        return booking.map(BookingMapper::toDto).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByUser(String email) {
        User user = userService.findByEmail(email);
        return bookingRepository.findAllByUserId(user.getId()).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(Long bookingId, String currentUserEmail) {
        User user = userService.findByEmail(currentUserEmail);
        if (user == null) {
            throw new RuntimeException("User not found: " + currentUserEmail);
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: This booking belongs to another user (ID: "
                    + booking.getUser().getId() + ", yours: " + user.getId() + ")");
        }

        // Capture details within transaction
        Room room = booking.getRoom();
        if (room == null) {
            throw new RuntimeException("Database Integrity Error: Booking has no associated room");
        }

        User roomOwner = room.getPostedBy();
        String roomTitle = room.getTitle();
        java.time.LocalDate checkInDate = booking.getCheckInDate();
        String userName = user.getName();
        String userEmail = user.getEmail();

        String ownerEmail = (roomOwner != null) ? roomOwner.getEmail() : null;
        String ownerName = (roomOwner != null) ? roomOwner.getName() : "Owner";

        // Perform deletion
        bookingRepository.delete(booking);

        // Send emails
        try {
            bookingMailService.sendBookingCancellationEmails(userName, userEmail, roomTitle, checkInDate);
            if (ownerEmail != null) {
                bookingMailService.sendBookingCancellationNotificationToOwner(ownerEmail, ownerName, roomTitle,
                        userName, checkInDate);
            }
        } catch (Exception e) {
            System.err.println("WARNING: Failed to send cancellation emails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteBooking(Long bookingId) {
        try {
            bookingRepository.deleteById(bookingId);
        } catch (Exception e) {
            System.err.println("ERROR in BookingService.deleteBooking: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
