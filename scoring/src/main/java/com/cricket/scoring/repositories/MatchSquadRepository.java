package com.cricket.scoring.repositories;

import com.cricket.scoring.entities.MatchSquad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchSquadRepository extends JpaRepository<MatchSquad, Long> {
}
