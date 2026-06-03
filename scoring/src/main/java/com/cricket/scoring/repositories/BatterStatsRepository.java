package com.cricket.scoring.repositories;

import com.cricket.scoring.entities.BatterStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatterStatsRepository extends JpaRepository<BatterStats, Long> {

    List<BatterStats> findByPlayerId(Long playerId);

    List<BatterStats> findByTournamentId(Long tournamentId);

    BatterStats findByTournamentIdAndPlayerId(Long tournamentId, Long playerId);

    Boolean existsByPlayerId(Long playerId);
}
