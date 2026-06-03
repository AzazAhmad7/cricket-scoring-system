package com.cricket.scoring.services;

import com.cricket.scoring.dtos.PointsTableDTO;
import com.cricket.scoring.dtos.ResponseFiles.LeagueTable;
import com.cricket.scoring.dtos.ResponseFiles.MatchState;
import com.cricket.scoring.dtos.ResponseFiles.SetupFile;
import com.cricket.scoring.entities.Tournament;

import java.util.List;

public interface PointsTableService {
    List<PointsTableDTO> getPointsTable(Long tournamentId);
    void saveAllPointsTable(List<PointsTableDTO> pointsTableDTOList);
    void generatePointsTable(Long tournamentId);
    LeagueTable updateLeagueTable(LeagueTable leagueTable, MatchState matchState, SetupFile setupFile);

}
