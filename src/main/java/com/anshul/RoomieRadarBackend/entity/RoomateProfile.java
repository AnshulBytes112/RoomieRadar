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
@Table(name = "roommate_profiles")
public class RoomateProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;

    @Column
    private int age;

    @Column
    private String occupation;

    @ElementCollection
    @CollectionTable(name = "roommate_profile_lifestyle", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "lifestyle")
    private List<String> lifestyle;

    @Column
    private String budget;

    @Column
    private String location;

    @Column
    private String bio;

    @ElementCollection
    @CollectionTable(name = "roommate_profile_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "interests")
    private List<String> interests;

    @Column
    private String avatar;

    @Column
    private String gender;

    @Column
    private String housingStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Column(nullable = false)
    private boolean deleted = false;
}
