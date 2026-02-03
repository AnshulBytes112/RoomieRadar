package com.anshul.RoomieRadarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDTO {
    private Long userId;
    private String name;
    private String email;
    private String username;
    private String phone;
    private String avatar;

    //(optional)
    private Integer age;
    private String occupation;
    private List<String> lifestyle;
    private String budget;
    private String location;
    private String bio;
    private List<String> interests;
    private String housingStatus;
    private String gender;

    private boolean hasRoommateProfile;
}
