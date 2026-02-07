package com.anshul.RoomieRadarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String gender;
    private Integer age;
    private boolean emailVerified;

    // Nested profile data to match frontend expectations
    private RoomateProfileInfo roomateProfile;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomateProfileInfo {
        private Long id;
        private String avatar;
        private String occupation;
        private boolean deleted;
    }
}
