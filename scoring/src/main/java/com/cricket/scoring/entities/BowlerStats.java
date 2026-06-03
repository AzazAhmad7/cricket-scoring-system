package com.cricket.scoring.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BowlerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private Integer matches = 0;

    private Integer innings = 0;

    private Integer wickets = 0;

    private Integer ballsBowled = 0;

    private Integer runsConceded = 0;

    private Integer maidens = 0;

    private Integer threeWicketHauls = 0;

    private Integer fiveWicketHauls = 0;

    private Integer bestBowlingWickets = 0;

    private Integer bestBowlingRuns = 0;

    private Double economy = 0.0;

    private Double average = 0.0;

    private Double strikeRate = 0.0;

    // Getters and Setters
}
