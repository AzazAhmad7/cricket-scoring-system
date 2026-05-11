package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Squad {
    private Playing11 homeTeamPlaying11;
    private Substitutes homeTeamSubstitutes;
    private BenchPlayers homeTeamBenchPlayers;
    private Playing11 awayTeamPlaying11;
    private Substitutes awayTeamSubstitutes;
    private BenchPlayers awayTeamBenchPlayers;
}
