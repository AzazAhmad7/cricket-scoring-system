package com.cricket.scoring.services;

import com.cricket.scoring.dtos.TeamDTO;
import com.cricket.scoring.entities.Team;

import java.util.List;

public interface TeamService {
    public TeamDTO createTeam(TeamDTO teamDTO);
    public TeamDTO getTeam(Long teamId);
    public List<TeamDTO> getAllTeams();
    public void deleteTeam(Long teamId);
    public TeamDTO updateTeam(Long teamId, TeamDTO teamDTO);
}
