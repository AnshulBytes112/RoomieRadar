package com.anshul.RoomieRadarBackend.Mapper;

import com.anshul.RoomieRadarBackend.dto.RoomDto;
import com.anshul.RoomieRadarBackend.entity.Room;

public class RoomMapper {
    public static RoomDto toDto(Room room) {
        return new RoomDto(
                room.getId(),
                room.getTitle(),
                room.getLocation(),
                room.getPrice(),
                room.getArea(),
                room.getBedrooms(),
                room.getBathrooms(),
                room.getImages(),
                room.getTags(),
                room.getDescription(),
                room.getAmenities(),
                room.getAvailaibleFrom(),
                room.getDeposit(),
                room.getMaintenance(),
                room.getParking(),
                room.getPetFriendly(),
                room.getFurnished(),
                room.getType().toString(),
                room.getContactNumber(),
                room.getContactEmail(),
                new RoomDto.PostedByDto(
                        room.getPostedBy().getId(),
                        room.getPostedBy().getName(),
                        room.getPostedBy().getEmail()
                )
        );
    }
}
