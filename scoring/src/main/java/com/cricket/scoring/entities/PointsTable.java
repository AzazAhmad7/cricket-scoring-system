package com.cricket.scoring.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "points_table",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tournament_team_points",
                        columnNames = {
                                "tournament_id",
                                "team_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_points_tournament",
                        columnList = "tournament_id"
                ),
                @Index(
                        name = "idx_points_team",
                        columnList = "team_id"
                ),
                @Index(
                        name = "idx_points_nrr",
                        columnList = "netRunRate"
                ),
                @Index(
                        name = "idx_points_points",
                        columnList = "points"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Tournament
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tournament_id",
            nullable = false
    )
    @JsonIgnore
    private Tournament tournament;

    /*
     * Team
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "team_id",
            nullable = false
    )
    private Team team;

    /*
     * Matches
     */
    @Builder.Default
    private Integer matchesPlayed = 0;

    @Builder.Default
    private Integer matchesWon = 0;

    @Builder.Default
    private Integer matchesLost = 0;

    @Builder.Default
    private Integer matchesTied = 0;

    @Builder.Default
    private Integer noResults = 0;

    /*
     * Points
     */
    @Builder.Default
    private Integer points = 0;

    /*
     * Net Run Rate
     */
    @Builder.Default
    private Double netRunRate = 0.0;

    /*
     * NRR Calculation Data
     */
    @Builder.Default
    private Integer runsScored = 0;

    @Builder.Default
    private Integer ballsFaced = 0;

    @Builder.Default
    private Integer runsConceded = 0;

    @Builder.Default
    private Integer ballsBowled = 0;
}