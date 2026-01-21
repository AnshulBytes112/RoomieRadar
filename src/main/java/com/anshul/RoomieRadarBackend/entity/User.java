package com.anshul.RoomieRadarBackend.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private RoomateProfile roomateProfile;

    @OneToMany(mappedBy = "postedBy")
    @JsonManagedReference
    private List<Room> rooms;

    @OneToMany(mappedBy = "uploadedBy")
    private List<Image> images;
    @OneToMany(mappedBy = "user")
    private List<Favourite> favourites;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings;

    private String phone;
}
