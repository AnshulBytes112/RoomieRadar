package com.anshul.RoomieRadarBackend.Mapper;

import com.anshul.RoomieRadarBackend.dto.RoomateProfileDTO;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import java.util.ArrayList;

public class RoomateProfileMapper {
    public static RoomateProfileDTO toDto(RoomateProfile roomProfile) {
        return new RoomateProfileDTO(
                roomProfile.getId(),
                roomProfile.getUser().getId(),
                roomProfile.getName(),
                roomProfile.getAge(),
                roomProfile.getOccupation(),
                roomProfile.getLifestyle() != null ? new ArrayList<>(roomProfile.getLifestyle()) : null,
                roomProfile.getBudget(),
                roomProfile.getLocation(),
                roomProfile.getBio(),
                roomProfile.getInterests() != null ? new ArrayList<>(roomProfile.getInterests()) : null,
                roomProfile.getAvatar(),
                roomProfile.getHousingStatus(),
                roomProfile.getGender(),
                roomProfile.getInstagram(),
                roomProfile.isDeleted());
    }
}
