package com.anshul.RoomieRadarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSummaryDTO {
    private Long id;
    private String title;
    private String location;
    private double price;
    private List<String> images;
}
