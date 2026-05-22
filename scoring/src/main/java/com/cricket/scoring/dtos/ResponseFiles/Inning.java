package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inning {
    private Integer inningNumber;
    private Long strikerId;
    private Long nonStrikerId;
    private Integer nextBattingPosition;
    private Long teamId;
    private Long currentBowlerId;
    private ScoreSummary scoreSummary;
    private InningControlMetrices controlMetrics;
    private Extras extras;
    private BattingCard battingCard;
    private BowlingCard bowlingCard;
    private PartnershipCard partnershipCard;
    private OverProgression overProgression;
    private PhaseBreakDownTeam phaseBreakdown;
    private List<FallOfWicket> fallOfWickets = new ArrayList<>();
    private List<OverEvent> oversHistory = new ArrayList<>();
}
