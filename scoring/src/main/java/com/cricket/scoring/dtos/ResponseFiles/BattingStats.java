package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.dtos.BatterStatsDTO;
import com.cricket.scoring.entities.BatterStats;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BattingStats {
    private List<BatterStatsDTO> homeTeamPlayersStats;
    private List<BatterStatsDTO> awayTeamPlayersStats;
}
