package com.cricket.scoring.dtos;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchResultRequest {
    private Long winnerTeamId;

    private String resultText;
    // India won by 5 wickets

    private String winType;
    // RUNS / WICKETS / TIE / NR

    private Integer margin;

    private Long playerOfMatchId;

    private Boolean superOverPlayed;
}
