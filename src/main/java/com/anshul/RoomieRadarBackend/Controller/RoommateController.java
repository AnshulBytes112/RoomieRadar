package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Service.RoommateService;
import com.anshul.RoomieRadarBackend.dto.RoomateProfileDTO;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.RoommateProfileRepository;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/roommates")
public class RoommateController {

    @Autowired
    private RoommateProfileRepository roommateProfileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoommateService roommateService;

    @PostMapping
    private ResponseEntity<?> createRoommate(@RequestBody RoomateProfile roomateProfile,
            Authentication authentication) {
        try {

            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not found"));
            }
            Optional<RoomateProfile> existingProfile = roommateProfileRepository.findByUser(user);
            if (existingProfile.isPresent()) {
                throw new IllegalStateException("User already has a roommate profile");
            }
            RoomateProfileDTO createdProfile = roommateService.createRoommate(roomateProfile, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating roommate profile: " + e.getMessage()));
        }
    }

    @GetMapping
    private ResponseEntity<List<RoomateProfileDTO>> getAllRoommates() {
        try {
            return ResponseEntity.ok().body(roommateService.getAllRoommates());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoommate(@PathVariable Long id, @RequestBody RoomateProfile profile,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
            }
            RoomateProfileDTO updatedProfile = roommateService.updateRoommate(id, profile, user);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

}
