package com.cricket.scoring.services;

import com.cricket.scoring.dtos.ResponseFiles.*;

import java.util.List;

public interface ScoreCardService {

    //BATTING CARD REALATED METHODS
    public BatterCard updateBattingCard(MatchState matchState, Inning inning, Event event);
    public ContextMetrics updateContextMetricsOfBatter(BatterCard batterCard, Event event, String entry, String exit);
    public ControlMetrics updateControlMetricsOfBatter(BatterCard batterCard, Event event);
    public PhaseBreakdownOfPlayer updatePhaseBreakdownOfBatter(MatchState matchState, BatterCard batterCard, Event event);
    public ScoringStats updateScoringStatsOfBatter(BatterCard batterCard, Event event);

    //BOWLING CARD RELATED METHODS
    public BowlingCard updateBowlingCard(MatchState matchState, Inning inning, Event event);

    //PARTNERSHIP CARD RELATED METHODS
    public PartnershipCard createPartnershipCard(Long playerId, MatchState matchState, Inning inning, Event event);
    public void updatePartnership(MatchState matchState, Inning inning, Event event);

    //OVER PROGRESSION
    public OverProgression createOverProgression(MatchState matchState, Inning inning, Event event);
    public OverProgression updateOverProgression(MatchState matchState, Inning inning, Event event);

    public PhaseBreakDownTeam updatePhaseBreakDown(MatchState matchState, Inning inning, Event event);

    public List<FallOfWicket> updateFOW(Long playerId, MatchState matchState, Inning inning, Event event);

    public InningControlMetrices updateInningControlMetrics(MatchState matchState, Inning inning, Event event);
}
