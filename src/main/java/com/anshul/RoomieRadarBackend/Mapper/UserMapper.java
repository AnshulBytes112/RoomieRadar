package com.anshul.RoomieRadarBackend.Mapper;

import com.anshul.RoomieRadarBackend.dto.UserDTO;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;

public class UserMapper {
    public static UserDTO toDto(User user) {
        if (user == null)
            return null;

        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .gender(user.getGender())
                .age(user.getAge())
                .emailVerified(user.isEmailVerified())
                .build();

        // Handle nested RoommateProfile safely
        RoomateProfile profile = user.getRoomateProfile();
        if (profile != null) {
            dto.setRoomateProfile(UserDTO.RoomateProfileInfo.builder()
                    .id(profile.getId())
                    .avatar(profile.getAvatar())
                    .occupation(profile.getOccupation())
                    .deleted(profile.isDeleted())
                    .build());
        }

        return dto;
    }
}
