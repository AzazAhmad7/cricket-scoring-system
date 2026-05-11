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
    private Long battingTeamId;
    private ScoreSummary scoreSummary;
    private Extras extras;
    private BattingCard battingCard;
    private BowlingCard bowlingCard;
    private PartnershipCard partnershipCard;
    private OverProgression overProgression;
    private PhaseBreakDownTeam phaseBreakdown;
    private List<FallOfWicket> fallOfWickets = new ArrayList<>();
    private List<OverEvent> oversHistory = new ArrayList<>();
}
