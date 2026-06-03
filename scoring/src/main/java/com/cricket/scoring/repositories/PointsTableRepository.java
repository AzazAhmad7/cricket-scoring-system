package com.cricket.scoring.repositories;

import com.cricket.scoring.dtos.PointsTableDTO;
import com.cricket.scoring.entities.PointsTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsTableRepository extends JpaRepository<PointsTable, Long> {
    List<PointsTable> findByTournamentId(Long tournamentId);

}
