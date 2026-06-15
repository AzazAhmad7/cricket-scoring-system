package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.AddTeamsToTournamentRequest;
import com.cricket.scoring.dtos.CreateTournamentResponse;
import com.cricket.scoring.dtos.TeamDTO;
import com.cricket.scoring.dtos.TournamentTeamDTO;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.Tournament;
import com.cricket.scoring.entities.TournamentTeam;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.repositories.TournamentRepository;
import com.cricket.scoring.repositories.TournamentTeamRepository;
import com.cricket.scoring.services.TeamService;
import com.cricket.scoring.services.TournamentService;
import com.cricket.scoring.services.TournamentTeamService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentTeamServiceImpl implements TournamentTeamService {
    private final ModelMapper modelMapper;
    private final TeamService teamService;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final TournamentRepository tournamentRepository;

    @Override
    public List<TournamentTeamDTO> getAllTeamsOfTournament(Long tournamentId) {
        List<TournamentTeam> tournamentTeam = tournamentTeamRepository.findByTournamentId(tournamentId);
        return tournamentTeam.stream()
                .map((element) -> modelMapper.map(element, TournamentTeamDTO.class))
                .collect(Collectors.toList());
    }

    public TournamentTeamDTO getTournamentTeamById(Long tournamentId) {
        TournamentTeam tournamentTeam = tournamentTeamRepository.findById(tournamentId).orElseThrow(()-> new ResourceNotFoundException("Tournament not found"));
        return modelMapper.map(tournamentTeam, TournamentTeamDTO.class);
    }

    @Transactional
    public void addTeams(AddTeamsToTournamentRequest request) {
        Tournament tournament = tournamentRepository.findById(request.getTournamentId()).orElseThrow(()-> new ResourceNotFoundException("Tournament not found "+request.getTournamentId()));

        for(Long teamId : request.getTeamIds()) {

            TeamDTO teamDTO = teamService.getTeam(teamId);

            boolean exists = tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(),teamDTO.getId());

            if(exists){
                continue;
            }

            TournamentTeam tournamentTeam =
                    TournamentTeam.builder()
                            .tournament(modelMapper.map(tournament, Tournament.class))
                            .team(modelMapper.map(teamDTO, Team.class))
                            .build();

            tournamentTeamRepository.save(tournamentTeam);
        }
    }
}
