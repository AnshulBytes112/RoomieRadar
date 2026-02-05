package com.anshul.RoomieRadarBackend.Mapper;

import com.anshul.RoomieRadarBackend.dto.RoomateProfileDTO;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;

public class RoomateProfileMapper {
    public static RoomateProfileDTO toDto(RoomateProfile roomProfile) {
        return new RoomateProfileDTO(
                roomProfile.getId(),
                roomProfile.getUser().getId(),
                roomProfile.getName(),
                roomProfile.getAge(),
                roomProfile.getOccupation(),
                roomProfile.getLifestyle(),
                roomProfile.getBudget(),
                roomProfile.getLocation(),
                roomProfile.getBio(),
                roomProfile.getInterests(),
                roomProfile.getAvatar(),
                roomProfile.getHousingStatus(),
                roomProfile.getGender(),
                roomProfile.getInstagram(),
                roomProfile.isDeleted());
    }
}
