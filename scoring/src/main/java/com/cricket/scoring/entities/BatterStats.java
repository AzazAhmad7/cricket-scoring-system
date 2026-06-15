package com.cricket.scoring.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "batter_stats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_batter_stats_player_tournament",
                        columnNames = {
                                "player_id",
                                "tournament_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_batter_player",
                        columnList = "player_id"
                ),
                @Index(
                        name = "idx_batter_tournament",
                        columnList = "tournament_id"
                ),
                @Index(
                        name = "idx_batter_runs",
                        columnList = "runs"
                ),
                @Index(
                        name = "idx_batter_average",
                        columnList = "average"
                ),
                @Index(
                        name = "idx_batter_strike_rate",
                        columnList = "strikeRate"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatterStats {

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
    private Integer notOuts = 0;

    @Builder.Default
    private Integer runs = 0;

    @Builder.Default
    private Integer balls = 0;

    @Builder.Default
    private Integer fours = 0;

    @Builder.Default
    private Integer sixes = 0;

    @Builder.Default
    private Integer fifties = 0;

    @Builder.Default
    private Integer hundreds = 0;

    @Builder.Default
    private Integer ducks = 0;

    @Builder.Default
    private Integer highestScore = 0;

    /*
     * Derived Stats
     */
    @Builder.Default
    private Double strikeRate = 0.0;

    @Builder.Default
    private Double average = 0.0;
}