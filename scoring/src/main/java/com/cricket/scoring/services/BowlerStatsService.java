package com.cricket.scoring.services;

import com.cricket.scoring.dtos.BatterStatsDTO;
import com.cricket.scoring.dtos.BowlerStatsDTO;
import com.cricket.scoring.entities.BowlerStats;

import java.util.Collection;
import java.util.List;

public interface BowlerStatsService {
    List<BowlerStatsDTO> getAllBowlerStatsByPlayerId(Long playerId);
    List<BowlerStatsDTO> getAllBowlerStatsByTournamentId(Long tournamentId);
    List<BowlerStatsDTO> getAllBowlerStats();
    BowlerStatsDTO getBowlerStatsOfPlayer(Long playerId, Long tournamentId);
    Boolean statsExistByPlayerId(Long playerId);
    BowlerStatsDTO addBowlerStats(BowlerStatsDTO bowlerStatsDTO);
}
