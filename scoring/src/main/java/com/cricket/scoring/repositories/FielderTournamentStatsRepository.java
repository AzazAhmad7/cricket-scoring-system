package com.cricket.scoring.repositories;

import com.cricket.scoring.dtos.FielderTournamentStatsDTO;
import com.cricket.scoring.entities.FielderTournamentStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FielderTournamentStatsRepository extends JpaRepository<FielderTournamentStats, Long> {
    List<FielderTournamentStats> findByPlayerId(Long playerId);

    FielderTournamentStats findByTournamentIdAndPlayerId(Long tournamentId, Long playerId);

    List<FielderTournamentStats> findByTournamentId(Long tournamentId);
}
