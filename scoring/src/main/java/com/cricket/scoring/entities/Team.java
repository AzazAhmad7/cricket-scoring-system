package com.cricket.scoring.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="teams")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name; // India

    private String shortName; // IND

    private String nickname;

    private String country;

    private String logoUrl;

    private String primaryColor;

    private String coachName;

    private Integer ranking;

    private Boolean active;


    /*
      matches where team is home side
    */
    @OneToMany(mappedBy = "homeTeam")
    private List<Match> homeMatches = new ArrayList<>();


    /*
      matches where team is away side
    */
    @OneToMany(mappedBy = "awayTeam")
    private List<Match> awayMatches = new ArrayList<>();


    /*
      squad players
    */
    @OneToMany(mappedBy="team")
    private List<Player> players = new ArrayList<>();
}
