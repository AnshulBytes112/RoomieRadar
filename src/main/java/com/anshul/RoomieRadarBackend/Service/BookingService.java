package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.dto.BookingRequest;
import com.anshul.RoomieRadarBackend.entity.Booking;
import com.anshul.RoomieRadarBackend.entity.Room;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.BookingRepository;
import com.anshul.RoomieRadarBackend.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    UserService userService;
    @Autowired
    RoomRepository roomRepository;

     @Autowired
    BookingRepository bookingRepository;
    public Booking createBooking(BookingRequest bookingRequest, Long RoomId, User user) {
        System.out.println("DEBUG: BookingService.createBooking called with RoomId: " + RoomId + ", User: " + user.getName());
        
        try {
            Room room = roomRepository.getReferenceById(RoomId);
            System.out.println("DEBUG: Found room: " + room.getTitle() + " (ID: " + room.getId() + ")");
            
            User roomCreator = room.getPostedBy();
            System.out.println("DEBUG: Room posted by: " + roomCreator.getName());
            
            Booking booking = new Booking();
            booking.setRoom(room);
            booking.setUser(user);
            booking.setCheckInDate(bookingRequest.getCheckInDate()); // Sets CheckInDate field
            booking.setCheckInDateField(bookingRequest.getCheckInDate()); // Set checkInDate field
            booking.setPhone(bookingRequest.getPhone());
            booking.setMessage(bookingRequest.getMessage());
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setCreatedAt(LocalDateTime.now()); // Set current timestamp
            
            System.out.println("DEBUG: Saving booking to database...");
            Booking savedBooking = bookingRepository.save(booking);
            System.out.println("DEBUG: Booking saved with ID: " + savedBooking.getId());
            
            return savedBooking;
        } catch (Exception e) {
            System.err.println("ERROR in BookingService.createBooking: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be caught by controller
        }
    }

    public User getRoomCreator(Long roomId) {
        Room room = roomRepository.getReferenceById(roomId);
        return room.getPostedBy();
    }

    public Booking getBooking(String name) {
        User user = userService.findByUsername(name);
        Optional<Booking> booking = bookingRepository.findByUserId(user.getId());
        return booking.orElse(null);
    }

    public List<Booking> getBookingsByUser(String username) {
        User user = userService.findByUsername(username);
        return bookingRepository.findAllByUserId(user.getId());
    }

    public void deleteBooking(Long bookingId) {
        try{
            bookingRepository.deleteById(bookingId);

        }
        catch (Exception e){
            System.err.println("ERROR in BookingService.deleteBooking: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
