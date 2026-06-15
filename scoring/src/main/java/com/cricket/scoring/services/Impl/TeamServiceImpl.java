package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.TeamDTO;
import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.exceptions.RuntimeConflictException;
import com.cricket.scoring.repositories.TeamRepository;
import com.cricket.scoring.services.TeamService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final ModelMapper modelMapper;

    @Override
    public TeamDTO createTeam(TeamDTO teamDTO) {
        Team team = Team.builder()
                .name(teamDTO.getName())
                .nickname(teamDTO.getNickname())
                .shortName(teamDTO.getShortName())
                .country(teamDTO.getCountry())
                .logoUrl(teamDTO.getLogoUrl())
                .primaryColor(teamDTO.getPrimaryColor())
                .coachName(teamDTO.getCoachName())
                .ranking(teamDTO.getRanking())
                .active(teamDTO.getActive())
                .build();
        return modelMapper.map(teamRepository.save(team), TeamDTO.class);
    }

    @Override
    public TeamDTO getTeam(Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new ResourceAccessException("Team not found with id " + teamId));
        return modelMapper.map(team, TeamDTO.class);
    }

    @Override
    public List<TeamDTO> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream().map(team -> modelMapper.map(team, TeamDTO.class)).toList();
    }

    @Override
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new ResourceAccessException("Team not found with id " + teamId));
        if (!team.getPlayers().isEmpty()) {
            throw new RuntimeConflictException(
                    "Team contains players. Remove or transfer players first."
            );
        }

        teamRepository.delete(team);
    }

    @Override
    public TeamDTO updateTeam(Long teamId, TeamDTO teamDTO) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new ResourceAccessException("Team not found with id " + teamId));
        if(teamDTO.getName() != null) team.setName(teamDTO.getName());
        if(teamDTO.getNickname() != null) team.setNickname(teamDTO.getNickname());
        if(teamDTO.getShortName() != null) team.setShortName(teamDTO.getShortName());
        if(teamDTO.getCountry() != null) team.setCountry(teamDTO.getCountry());
        if(teamDTO.getLogoUrl() != null) team.setLogoUrl(teamDTO.getLogoUrl());
        if(teamDTO.getPrimaryColor() != null) team.setPrimaryColor(teamDTO.getPrimaryColor());
        if(teamDTO.getCoachName() != null) team.setCoachName(teamDTO.getCoachName());
        if(teamDTO.getRanking() != null) team.setRanking(teamDTO.getRanking());
        if(teamDTO.getActive() != null) team.setActive(teamDTO.getActive());
        return modelMapper.map(teamRepository.save(team), TeamDTO.class);
    }
}
