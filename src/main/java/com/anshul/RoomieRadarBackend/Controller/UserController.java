package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.dto.UserProfileDTO;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.RoommateProfileRepository;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoommateProfileRepository roommateProfileRepository;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "User ID is required"));
        }
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            RoomateProfile roomateProfile = roommateProfileRepository.findByUser(user).orElse(null);

            UserProfileDTO.UserProfileDTOBuilder builder = UserProfileDTO.builder()
                    .userId(user.getId())
                    .name(user.isDeleted() ? "Deleted User" : user.getName())
                    .email(user.isDeleted() ? "" : user.getEmail())
                    .phone(user.isDeleted() ? "" : user.getPhone())
                    .deleted(user.isDeleted())
                    .hasRoommateProfile(roomateProfile != null);

            if (roomateProfile != null && !user.isDeleted()) {
                builder.avatar(roomateProfile.getAvatar())
                        .age(roomateProfile.getAge())
                        .occupation(roomateProfile.getOccupation())
                        .lifestyle(roomateProfile.getLifestyle())
                        .budget(roomateProfile.getBudget())
                        .location(roomateProfile.getLocation())
                        .bio(roomateProfile.getBio())
                        .interests(roomateProfile.getInterests())
                        .housingStatus(roomateProfile.getHousingStatus())
                        .gender(roomateProfile.getGender());
            }

            return ResponseEntity.ok(builder.build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/profile/deactivate")
    public ResponseEntity<?> deactivateAccount(org.springframework.security.core.Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setDeleted(true);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Account deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
