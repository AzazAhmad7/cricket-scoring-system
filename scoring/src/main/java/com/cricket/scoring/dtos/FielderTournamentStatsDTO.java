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
public class FielderTournamentStatsDTO {
    private Long id;

    private Long tournamentId;
    private Long playerId;

    private Integer catches = 0;

    private Integer runOuts = 0;

    private Integer stumpings = 0;
}
