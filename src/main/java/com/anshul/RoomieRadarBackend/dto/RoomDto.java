package com.anshul.RoomieRadarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto {
    private Long id;
    private String title;
    private String location;
    private int price;
    private String area;
    private int bedrooms;
    private int bathrooms;
    private List<String> images;
    private List<String> tags;
    private String description;
    private List<String> amenities;
    private String availaibleFrom;
    private String deposit;
    private String maintenance;
    private Boolean parking;
    private Boolean petFriendly;
    private Boolean furnished;
    private String type;
    private String contactNumber;
    private String contactEmail;
    private PostedByDto postedBy;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostedByDto {
        private Long id;
        private String name;
        private String email;
    }
}
