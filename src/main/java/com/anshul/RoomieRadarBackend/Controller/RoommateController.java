package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Service.RoommateService;
import com.anshul.RoomieRadarBackend.dto.RoomateProfileDTO;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.RoommateProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
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

            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);
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

    @GetMapping("/search")
    private ResponseEntity<?> searchRoommates(
            @RequestParam(required = false) String ageRange,
            @RequestParam(required = false) String lifestyle,
            @RequestParam(required = false) String budget,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String occupation,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RoomateProfileDTO> profiles = roommateService.searchRoommates(ageRange, lifestyle, budget, location,
                    occupation, gender, pageable);
            return ResponseEntity.ok().body(profiles);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage(), "trace", e.getStackTrace()[0].toString()));
        }
    }

    @GetMapping
    private ResponseEntity<Page<RoomateProfileDTO>> getAllRoommates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok().body(roommateService.getAllRoommates(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    private ResponseEntity<?> getRoommateById(@PathVariable Long id) {
        try {
            RoomateProfileDTO profile = roommateService.getRoommateById(id);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoommate(@PathVariable Long id, @RequestBody RoomateProfile profile,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
            }
            RoomateProfileDTO updatedProfile = roommateService.updateRoommate(id, profile, user);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            RoomateProfile profile = roommateProfileRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Profile not found"));
            return ResponseEntity.ok(roommateService.getRoommateById(profile.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoommateProfile(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            boolean deleted = roommateService.deleteRoommateProfile(id, user);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Roommate profile deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Unauthorized to delete this profile"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}
