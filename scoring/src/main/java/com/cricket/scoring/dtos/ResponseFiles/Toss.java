package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.entities.enums.TossDecision;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Toss {
    private Long winnerId;
    private Long battingTeamId;
    private Long bowlingTeamId;
    private String winnerName;
    private TossDecision tossDecision;
}
