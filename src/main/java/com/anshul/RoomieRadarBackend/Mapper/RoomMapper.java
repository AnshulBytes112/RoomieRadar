package com.anshul.RoomieRadarBackend.Mapper;

import com.anshul.RoomieRadarBackend.dto.RoomDto;
import com.anshul.RoomieRadarBackend.entity.Room;
import java.util.ArrayList;

public class RoomMapper {
        public static RoomDto toDto(Room room) {
                if (room == null)
                        return null;

                return new RoomDto(
                                room.getId(),
                                room.getTitle(),
                                room.getLocation(),
                                room.getPrice(),
                                room.getArea(),
                                room.getBedrooms(),
                                room.getBathrooms(),
                                room.getImages() != null ? new ArrayList<>(room.getImages()) : null,
                                room.getTags() != null ? new ArrayList<>(room.getTags()) : null,
                                room.getDescription(),
                                room.getHouseRules(),
                                room.getHouseDetails(),
                                room.getGenderPreference(),
                                room.getAmenities() != null ? new ArrayList<>(room.getAmenities()) : null,
                                room.getAvailaibleFrom(),
                                room.getDeposit(),
                                room.getMaintenance(),
                                room.getParking(),
                                room.getPetFriendly(),
                                room.getFurnished(),
                                room.getType() != null ? room.getType().toString() : null,
                                room.getContactNumber(),
                                room.getContactEmail(),
                                room.getMapLink(),
                                room.getTotalOccupancy() != null ? room.getTotalOccupancy() : 1,
                                room.getOccupiedCount() != null ? room.getOccupiedCount() : 0,
                                room.getPostedBy() != null ? new RoomDto.PostedByDto(
                                                room.getPostedBy().getId(),
                                                room.getPostedBy().getName(),
                                                room.getPostedBy().getEmail(),
                                                room.getPostedBy().getRoomateProfile() != null
                                                                ? room.getPostedBy().getRoomateProfile().getAvatar()
                                                                : null,
                                                room.getPostedBy().getRoomateProfile() != null
                                                                ? room.getPostedBy().getRoomateProfile().getOccupation()
                                                                : null,
                                                room.getPostedBy().isDeleted())
                                                : null,
                                room.isDeleted());
        }
}
