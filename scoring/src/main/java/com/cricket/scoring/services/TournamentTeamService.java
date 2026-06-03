package com.cricket.scoring.services;

import com.cricket.scoring.dtos.AddTeamsToTournamentRequest;
import com.cricket.scoring.dtos.TournamentTeamDTO;
import com.cricket.scoring.entities.TournamentTeam;

import java.util.List;

public interface TournamentTeamService {
    List<TournamentTeamDTO> getAllTeamsOfTournament(Long tournamentId);

    void addTeams(AddTeamsToTournamentRequest request);
}
