package com.cricket.scoring.repositories;

import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByTeam(Team team);
}
