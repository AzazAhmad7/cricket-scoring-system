package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.dtos.BatterStatsDTO;
import com.cricket.scoring.entities.BatterStats;
import com.cricket.scoring.entities.enums.MatchStatus;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchState {
    private Long matchId;
    private MatchStatus matchStatus;
    private String matchName;
    private Integer currentInningNumber;
    private Long battingTeamId;
    private ScoreCard scoreCard;
    private TargetDTO targetDTO;
    private MatchResultDTO matchResultDTO;
}
