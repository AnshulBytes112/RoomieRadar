package com.anshul.RoomieRadarBackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role;

    private Instant lastActive;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private RoomateProfile roomateProfile;

    @OneToMany(mappedBy = "postedBy")
    @JsonManagedReference
    private List<Room> rooms;

    @OneToMany(mappedBy = "uploadedBy")
    @JsonIgnore
    private List<Image> images;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Favourite> favourites;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Booking> bookings;

    private String phone;
}
