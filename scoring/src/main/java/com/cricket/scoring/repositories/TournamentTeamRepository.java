package com.cricket.scoring.repositories;

import com.cricket.scoring.entities.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {
    List<TournamentTeam> findByTournamentId(Long tournamentId);

    boolean existsByTournamentIdAndTeamId(Long id, Long id1);
}
