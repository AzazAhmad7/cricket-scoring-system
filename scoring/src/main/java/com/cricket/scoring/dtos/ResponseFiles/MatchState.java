package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchState {
    private Long matchId;
    private String matchStatus;
    private String matchName;
    private Integer currentInningNumber;

    private Long strikerId;

    private Long nonStrikerId;
    private Integer nextBattingPosition;

    private Long currentBowlerId;

    private ScoreCard scoreCard;
}
