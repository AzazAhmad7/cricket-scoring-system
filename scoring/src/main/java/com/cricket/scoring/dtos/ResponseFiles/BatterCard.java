package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatterCard {

    private PlayerInfo batter;

    private ScoringStats scoring;

    private DismissalInfo dismissal;

    private PhaseBreakdownOfPlayer phases;

    private ControlMetrics control;

    private ContextMetrics context;

    private Boolean onStrike;
    private Boolean isImpactIn;
    private Boolean isImpactOut;
}
