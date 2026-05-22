package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.entities.enums.MatchStatus;
import lombok.*;

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
