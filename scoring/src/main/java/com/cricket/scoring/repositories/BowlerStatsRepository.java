package com.cricket.scoring.repositories;

import com.cricket.scoring.entities.BatterStats;
import com.cricket.scoring.entities.BowlerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BowlerStatsRepository extends JpaRepository<BowlerStats, Integer> {
    List<BowlerStats> findByPlayerId(Long playerId);

    List<BowlerStats> findByTournamentId(Long tournamentId);

    BowlerStats findByTournamentIdAndPlayerId(Long tournamentId, Long playerId);

    Boolean existsByPlayerId(Long playerId);
}
