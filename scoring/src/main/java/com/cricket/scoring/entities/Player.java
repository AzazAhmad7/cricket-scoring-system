package com.cricket.scoring.entities;

import com.cricket.scoring.entities.enums.BattingStyle;
import com.cricket.scoring.entities.enums.BowlingStyle;
import com.cricket.scoring.entities.enums.PlayerRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalPlayerId;

    @Column(nullable=false)
    private String fullName;

    private String shortName;

    private Integer jerseyNumber;

    private LocalDate dateOfBirth;

    private String nationality;


    /*
      Batter, Bowler, All Rounder, WK
     */
    @Enumerated(EnumType.STRING)
    private PlayerRole role;


    @Enumerated(EnumType.STRING)
    private BattingStyle battingStyle;


    @Enumerated(EnumType.STRING)
    private BowlingStyle bowlingStyle;


    private Boolean captain;

    private Boolean wicketKeeper;
    private Integer battingOrder;

    private Boolean active;
    /*
       Player belongs to one team
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id")
    @JsonIgnore
    private Team team;

    /*
      Player can appear in many match squads
     */
    @OneToMany(mappedBy = "player")
    @JsonIgnore
    private List<MatchSquad> matchSquads = new ArrayList<>();


    // Career stats (optional)
    private Integer matchesPlayed;

    private Integer runs;

    private Integer wickets;

    private Double battingAverage;

    private Double bowlingAverage;
}
