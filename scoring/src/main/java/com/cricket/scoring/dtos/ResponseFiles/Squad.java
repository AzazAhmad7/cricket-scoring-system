package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.dtos.ImpactPlayerDTO;
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
    private ImpactPlayerDTO homeTeamImpactPlayerDTO;
    private Playing11 awayTeamPlaying11;
    private Substitutes awayTeamSubstitutes;
    private BenchPlayers awayTeamBenchPlayers;
    private ImpactPlayerDTO awayTeamImpactPlayerDTO;
}
