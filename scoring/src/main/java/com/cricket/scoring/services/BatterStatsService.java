package com.cricket.scoring.services;

import com.cricket.scoring.dtos.BatterStatsDTO;

import java.util.List;

public interface BatterStatsService {
    List<BatterStatsDTO> getAllBatterStatsByPlayerId(Long playerId);
    List<BatterStatsDTO> getAllBatterStatsByTournamentId(Long tournamentId);
    List<BatterStatsDTO> getAllBatterStats();
    BatterStatsDTO getBatterStatsOfPlayer(Long playerId, Long tournamentId);
    Boolean statsExistByPlayerId(Long playerId);
    BatterStatsDTO addBatterStats(BatterStatsDTO batterStatsDTO);
}
