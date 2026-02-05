package com.anshul.RoomieRadarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomateProfileDTO {
    private Long id;
    private Long userId;
    private String name;
    private int age;
    private String occupation;
    private java.util.List<String> lifestyle;
    private String budget;
    private String location;
    private String bio;
    private java.util.List<String> interests;
    private String avatar;
    private String housingStatus;
    private String gender;
    private String instagram;
    private boolean deleted;

}
