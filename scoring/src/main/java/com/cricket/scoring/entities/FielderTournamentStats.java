package com.cricket.scoring.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "fielder_tournament_stats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fielder_stats_player_tournament",
                        columnNames = {
                                "player_id",
                                "tournament_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_fielder_player",
                        columnList = "player_id"
                ),
                @Index(
                        name = "idx_fielder_tournament",
                        columnList = "tournament_id"
                ),
                @Index(
                        name = "idx_fielder_catches",
                        columnList = "catches"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FielderTournamentStats {

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

    /*
     * Fielding Statistics
     */
    @Builder.Default
    private Integer catches = 0;

    @Builder.Default
    private Integer runOuts = 0;

    @Builder.Default
    private Integer stumpings = 0;

    /*
     * Additional Useful Stats
     */
    @Builder.Default
    private Integer directHitRunOuts = 0;

    @Builder.Default
    private Integer assistedRunOuts = 0;
}