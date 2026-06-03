package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerStats {
    private BattingStats battingStats;
    private BowlingStats bowlingStats;
}
