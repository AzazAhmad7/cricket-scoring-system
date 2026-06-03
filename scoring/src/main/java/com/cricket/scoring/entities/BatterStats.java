package com.cricket.scoring.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatterStats {

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

    private Integer notOuts = 0;

    private Integer runs = 0;

    private Integer balls = 0;

    private Integer fours = 0;

    private Integer sixes = 0;

    private Integer fifties = 0;

    private Integer hundreds = 0;

    private Integer ducks = 0;

    private Integer highestScore = 0;

    private Double strikeRate = 0.0;

    private Double average = 0.0;

    // getters and setters
}
