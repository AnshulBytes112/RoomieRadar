package com.anshul.RoomieRadarBackend.Mapper;

import com.anshul.RoomieRadarBackend.dto.FavouriteDTO;
import com.anshul.RoomieRadarBackend.dto.RoomSummaryDTO;
import com.anshul.RoomieRadarBackend.entity.Favourite;
import com.anshul.RoomieRadarBackend.entity.Room;

public class FavouriteMapper {
    public static FavouriteDTO toDto(Favourite favourite) {
        if (favourite == null)
            return null;
        Room room = favourite.getRoom();

        return FavouriteDTO.builder()
                .id(favourite.getId())
                .room(room != null ? RoomSummaryDTO.builder()
                        .id(room.getId())
                        .title(room.getTitle())
                        .location(room.getLocation())
                        .price(room.getPrice())
                        .images(room.getImages() != null ? new java.util.ArrayList<>(room.getImages()) : null)
                        .build() : null)
                .build();
    }
}
