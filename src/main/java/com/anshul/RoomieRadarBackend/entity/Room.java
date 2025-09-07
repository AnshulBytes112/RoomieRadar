package com.anshul.RoomieRadarBackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})// To ignore lazy loading issues during JSON serialization
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private int price;

    @Column
    private String area;

    @Column(nullable = false)
    private int bedrooms;

    @Column(nullable = false)
    private int bathrooms;

    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url")
    private List<String> images;

    @ElementCollection
    @CollectionTable(name = "room_tags", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Enumerated(EnumType.STRING)
    private RoomType type; // "Private", "Shared", "Studio"

    private String description;
    @ElementCollection
    private List<String> amenities;

    private String availaibleFrom;
    private String deposit;
    private String maintenance;
    private Boolean parking;
    private Boolean petFriendly;
    private Boolean furnished;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_user_id", nullable = false)
    @JsonIgnoreProperties({"rooms","hibernateLazyInitializer", "handler"})
    private User postedBy;
    public enum RoomType {
        Private, Shared, Studio
    }
}
