package com.cricket.scoring.services;

import com.cricket.scoring.dtos.PlayerDTO;
import com.cricket.scoring.dtos.ResponseFiles.*;

import java.util.List;

public interface ScoringService {
    public void scoreBall(Event event);
    public BatterCard selectNewBatter(Long playerId, Event event);
    public BowlingCard selectNewBowler(Long playerId, Event event);
    public void switchStrike(MatchState matchState, Inning inning);
    public void endOver(Event event);
}
