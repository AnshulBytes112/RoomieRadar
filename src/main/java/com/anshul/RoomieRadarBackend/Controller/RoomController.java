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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("api/rooms")


public class RoomController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private RoomService roomService;
//    @GetMapping("/{id}")
//    private ResponseEntity<?> getRoomById(){
//
//    }
//    @GetMapping("/search")
//    public ResponseEntity<List<Room>> searchRooms(
//            @RequestParam(required = false) String location,
//            @RequestParam(required = false) String budget,
//            @RequestParam(required = false) String roomType,
//            @RequestParam(required = false) Integer bedrooms,
//            @RequestParam(required = false) Integer bathrooms) {
//
//
//    }

    @GetMapping
    private ResponseEntity<List<RoomDto>> getAllRooms(){
        List<RoomDto> rooms = roomService.getAllRooms()
                .stream()
                .map(RoomMapper::toDto) // map each Room to RoomDto
                .toList();
        return ResponseEntity.ok(rooms);
    }
//
    @PostMapping
    private ResponseEntity<?> createRoom(@RequestBody Room room,Authentication authentication){
        String username = authentication.getName();
        User user=userService.findByUsername(username);
        RoomDto saved = roomService.createRoom(room, user);
        return ResponseEntity.ok(saved);

    }
//
//    @PutMapping("/{id}")
//    private ResponseEntity<?> updateRoom(){
//
//    }
//
    @DeleteMapping("/{id}")
    private ResponseEntity<?> deleteRoom(@PathVariable Long id,Authentication authentication){
        String username = authentication.getName();

        boolean deleted = roomService.deleteRoom(id, username);

        if (deleted) {
            return ResponseEntity.ok("Room deleted successfully.");
        } else {
            return ResponseEntity.status(403).body("You are not authorized to delete this room.");
        }

    }
}
