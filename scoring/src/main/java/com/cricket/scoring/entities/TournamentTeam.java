package com.cricket.scoring.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "tournament_team",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tournament_team",
                        columnNames = {"tournament_id", "team_id"}
                )
        },
        indexes = {
                @Index(name = "idx_tt_tournament", columnList = "tournament_id"),
                @Index(name = "idx_tt_team", columnList = "team_id"),
                @Index(name = "idx_tt_group", columnList = "groupName")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentTeam {

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
     * Seeding
     */
    private Integer seed;

    /*
     * Group A, Group B, etc.
     */
    @Column(length = 50)
    private String groupName;
}