package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.Tournament;
import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BowlerStatsDTO {
    private Long id;
    private Long tournamentId;

    private Long playerId;

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
}
