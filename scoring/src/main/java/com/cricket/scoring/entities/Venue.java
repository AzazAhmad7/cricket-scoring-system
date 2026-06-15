package com.cricket.scoring.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "venues",
        indexes = {
                @Index(name = "idx_venue_name", columnList = "name"),
                @Index(name = "idx_venue_city", columnList = "city"),
                @Index(name = "idx_venue_country", columnList = "country")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 50)
    private String pitchType;

    private Integer straightBoundaryMeters;

    private Integer squareBoundaryMeters;

    private Integer capacity;

    @Column(length = 100)
    private String timeZone;

    /*
     * Matches hosted at this venue
     */
    @OneToMany(mappedBy = "venue")
    @JsonIgnore
    @Builder.Default
    private List<Match> matches = new ArrayList<>();
}