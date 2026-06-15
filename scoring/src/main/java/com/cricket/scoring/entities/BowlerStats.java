package com.cricket.scoring.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "bowler_stats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bowler_stats_player_tournament",
                        columnNames = {
                                "player_id",
                                "tournament_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_bowler_player",
                        columnList = "player_id"
                ),
                @Index(
                        name = "idx_bowler_tournament",
                        columnList = "tournament_id"
                ),
                @Index(
                        name = "idx_bowler_wickets",
                        columnList = "wickets"
                ),
                @Index(
                        name = "idx_bowler_economy",
                        columnList = "economy"
                ),
                @Index(
                        name = "idx_bowler_average",
                        columnList = "average"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BowlerStats {

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
     * Player
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "player_id",
            nullable = false
    )
    private Player player;

    @Builder.Default
    private Integer matches = 0;

    @Builder.Default
    private Integer innings = 0;

    @Builder.Default
    private Integer wickets = 0;

    @Builder.Default
    private Integer ballsBowled = 0;

    @Builder.Default
    private Integer runsConceded = 0;

    @Builder.Default
    private Integer maidens = 0;

    @Builder.Default
    private Integer threeWicketHauls = 0;

    @Builder.Default
    private Integer fiveWicketHauls = 0;

    /*
     * Best Bowling Figures
     * Example: 5/32
     */
    @Builder.Default
    private Integer bestBowlingWickets = 0;

    @Builder.Default
    private Integer bestBowlingRuns = 0;

    /*
     * Derived Statistics
     */
    @Builder.Default
    private Double economy = 0.0;

    @Builder.Default
    private Double average = 0.0;

    @Builder.Default
    private Double strikeRate = 0.0;
}