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
public class BatterStatsDTO {
    private Long id;
    private Long tournamentId;
    private Long playerId;

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
}
