package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.dto.UserProfileDTO;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.RoommateProfileRepository;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.ArrayList;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoommateProfileRepository roommateProfileRepository;

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email);
    }

    public User findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
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
                    .lifestyle(roomateProfile.getLifestyle() != null ? new ArrayList<>(roomateProfile.getLifestyle())
                            : null)
                    .budget(roomateProfile.getBudget())
                    .location(roomateProfile.getLocation())
                    .bio(roomateProfile.getBio())
                    .interests(roomateProfile.getInterests() != null ? new ArrayList<>(roomateProfile.getInterests())
                            : null)
                    .housingStatus(roomateProfile.getHousingStatus())
                    .gender(roomateProfile.getGender());
        }

        return builder.build();
    }

    @Transactional
    public void deactivateAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDeleted(true);
        userRepository.save(user);
    }
}
