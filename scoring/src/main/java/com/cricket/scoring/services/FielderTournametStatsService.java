package com.cricket.scoring.services;

import com.cricket.scoring.dtos.BatterStatsDTO;
import com.cricket.scoring.dtos.FielderTournamentStatsDTO;

import java.util.List;

public interface FielderTournametStatsService {
    List<FielderTournamentStatsDTO> getAllFielderStatsByPlayerId(Long playerId);
    List<FielderTournamentStatsDTO> getAllFielderStatsByTournamentId(Long tournamentId);
    List<FielderTournamentStatsDTO> getAllFielderStats();
    FielderTournamentStatsDTO getFielderStatsOfPlayer(Long playerId, Long tournamentId);
}
