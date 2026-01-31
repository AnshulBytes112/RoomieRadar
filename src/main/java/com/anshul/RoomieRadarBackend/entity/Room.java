package com.anshul.RoomieRadarBackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @Column(nullable = false, length = 500)
    private String location;

    @Column(nullable = false)
    private int price;

    @Column(length = 200)
    private String area;

    @Column(nullable = false)
    private int bedrooms;

    @Column(nullable = false)
    private int bathrooms;

    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> images;

    @ElementCollection
    @CollectionTable(name = "room_tags", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "tag", length = 100)
    private List<String> tags;

    @Enumerated(EnumType.STRING)
    private RoomType type; // "Private", "Shared", "Studio"

    @Column(length = 2000)
    private String description;
    @ElementCollection
    @CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "amenity", length = 200)
    private List<String> amenities;

    @Column(length = 100)
    private String availaibleFrom;
    @Column(length = 50)
    private String deposit;
    @Column(length = 50)
    private String maintenance;
    private Boolean parking;
    private Boolean petFriendly;
    private Boolean furnished;
    @Column(length = 20)
    private String contactNumber;
    @Column(length = 255)
    private String contactEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_user_id", nullable = false)
    @JsonIgnoreProperties({"rooms","hibernateLazyInitializer", "handler"})
    @JsonBackReference
    private User postedBy;
    public enum RoomType {
        Private, Shared, Studio,Hostel
    }
}
