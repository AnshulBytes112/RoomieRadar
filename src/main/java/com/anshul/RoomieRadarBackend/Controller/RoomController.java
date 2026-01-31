package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Mapper.RoomMapper;
import com.anshul.RoomieRadarBackend.Service.RoomService;
import com.anshul.RoomieRadarBackend.Service.UserDetailsServiceImpl;
import com.anshul.RoomieRadarBackend.Service.UserService;
import com.anshul.RoomieRadarBackend.dto.RoomDto;
import com.anshul.RoomieRadarBackend.entity.Room;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
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
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) Integer bathrooms,
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
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        RoomDto saved = roomService.createRoom(room, user);
        return ResponseEntity.ok(saved);

    }

    @GetMapping("/{id}")
    private ResponseEntity<?> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));

    }

    @GetMapping("/my-listings")
    public ResponseEntity<List<RoomDto>> getMyListings(Authentication authentication) {
        String username = authentication.getName();
        List<RoomDto> rooms = roomService.getRoomsByUser(username)
                .stream()
                .map(RoomMapper::toDto)
                .toList();
        return ResponseEntity.ok(rooms);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room room, Authentication authentication) {
        try {
            String username = authentication.getName();
            RoomDto updatedRoom = roomService.updateRoom(id, room, username);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    //
    @DeleteMapping("/{id}")
    private ResponseEntity<?> deleteRoom(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();

        boolean deleted = roomService.deleteRoom(id, username);

        if (deleted) {
            return ResponseEntity.ok("Room deleted successfully.");
        } else {
            return ResponseEntity.status(403).body("You are not authorized to delete this room.");
        }

    }
}
