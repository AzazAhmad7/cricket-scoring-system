package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.dtos.BowlerStatsDTO;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BowlingStats {
    private List<BowlerStatsDTO> homeTeamPlayerStats;
    private List<BowlerStatsDTO> awayTeamPlayerStats;
}
