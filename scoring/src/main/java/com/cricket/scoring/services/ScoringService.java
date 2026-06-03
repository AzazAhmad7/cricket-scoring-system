package com.cricket.scoring.services;

import com.cricket.scoring.dtos.ImpactPlayerDTO;
import com.cricket.scoring.dtos.PlayerDTO;
import com.cricket.scoring.dtos.ResponseFiles.*;

import java.util.List;

public interface ScoringService {
     void scoreBall(Event event);
     BatterCard selectNewBatter(Long playerId, Event event);
     BowlingCard selectNewBowler(Long playerId, Event event);
     boolean switchStrike(MatchState matchState);
     boolean swapStrikerDirectly(MatchState matchState);
     void endOver(Event event);
     void impactPlayer(ImpactPlayerDTO impactPlayerDTO, Long matchId);
     MatchState rebuildMatchState( Long matchId);
}
