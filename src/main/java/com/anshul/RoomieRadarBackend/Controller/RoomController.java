package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Mapper.RoomMapper;
import com.anshul.RoomieRadarBackend.Service.RoomService;
import com.anshul.RoomieRadarBackend.Service.UserDetailsServiceImpl;
import com.anshul.RoomieRadarBackend.Service.UserService;
import com.anshul.RoomieRadarBackend.dto.RoomDto;
import com.anshul.RoomieRadarBackend.entity.Room;
import com.anshul.RoomieRadarBackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("api/rooms")

public class RoomController {
    @Autowired
    private UserService userService;
    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private RoomService roomService;

    @GetMapping("/search")
    public ResponseEntity<Page<RoomDto>> searchRooms(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String budget,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String bedrooms,
            @RequestParam(required = false) String bathrooms,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomDto> rooms = roomService.searchRooms(location, budget, roomType, bedrooms, bathrooms, pageable)
                .map(RoomMapper::toDto);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping
    private ResponseEntity<Page<RoomDto>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomDto> rooms = roomService.getAllRooms(pageable)
                .map(RoomMapper::toDto);
        return ResponseEntity.ok(rooms);
    }

    //
    @PostMapping
    private ResponseEntity<?> createRoom(@RequestBody Room room, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        RoomDto saved = roomService.createRoom(room, user);
        return ResponseEntity.ok(saved);

    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(RoomMapper.toDto(room));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<List<RoomDto>> getMyListings(Authentication authentication) {
        String email = authentication.getName();
        List<RoomDto> rooms = roomService.getRoomsByUser(email)
                .stream()
                .map(RoomMapper::toDto)
                .toList();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RoomDto>> getRoomsByUserId(@PathVariable Long userId) {
        List<RoomDto> rooms = roomService.getRoomsByUserId(userId)
                .stream()
                .map(RoomMapper::toDto)
                .toList();
        return ResponseEntity.ok(rooms);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room room, Authentication authentication) {
        try {
            String email = authentication.getName();
            RoomDto updatedRoom = roomService.updateRoom(id, room, email);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    //
    @DeleteMapping("/{id}")
    private ResponseEntity<?> deleteRoom(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();

        boolean deleted = roomService.deleteRoom(id, email);

        if (deleted) {
            return ResponseEntity.ok("Room deleted successfully.");
        } else {
            return ResponseEntity.status(403).body("You are not authorized to delete this room.");
        }

    }
}
