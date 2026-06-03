package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.Tournament;
import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointsTableDTO {
    private Long id;
    private Long tournamentId;
    private Long teamId;

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
