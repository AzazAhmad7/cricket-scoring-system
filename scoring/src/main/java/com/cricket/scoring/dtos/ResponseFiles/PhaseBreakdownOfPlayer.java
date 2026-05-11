package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhaseBreakdownOfPlayer {
    private Integer powerPlayRuns;
    private Integer middleOverRuns;
    private Integer deathOverRuns;
}
