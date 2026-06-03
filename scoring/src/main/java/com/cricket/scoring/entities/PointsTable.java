package com.cricket.scoring.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private Integer matchesPlayed = 0;

    private Integer matchesWon = 0;

    private Integer matchesLost = 0;

    private Integer matchesTied = 0;

    private Integer noResults = 0;

    private Integer points = 0;

    private Double netRunRate = 0.0;

    private Integer runsScored = 0;

    private Integer ballsFaced = 0;

    private Integer runsConceded = 0;

    private Integer ballsBowled = 0;
}
