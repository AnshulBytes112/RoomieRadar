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

import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    UserService userService;
    @Autowired
    RoomRepository roomRepository;

     @Autowired
    BookingRepository bookingRepository;
    public Booking createBooking(BookingRequest bookingRequest, Long RoomId) {
        Room room = roomRepository.getReferenceById(RoomId);
        Optional<User> user = userService.getCurrentUser();

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user.orElse(null));
        booking.setCheckInDate(bookingRequest.getCheckInDate());
        booking.setPhone(bookingRequest.getPhone());
        booking.setMessage(bookingRequest.getMessage());
        booking.setStatus(Booking.BookingStatus.PENDING);
        return bookingRepository.save(booking);
    }
}
