package com.cricket.scoring.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String city;

    private String state;

    private String country;

    private String pitchType;

    private Integer straightBoundaryMeters;

    private Integer squareBoundaryMeters;

    private Integer capacity;

    private String timeZone;


    /*
       one venue hosts many matches
    */
    @OneToMany(mappedBy="venue")
    private List<Match> matches = new ArrayList<>();
}