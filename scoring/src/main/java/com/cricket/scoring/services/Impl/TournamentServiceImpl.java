package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.CreateTournamentResponse;
import com.cricket.scoring.dtos.CreateTournametRequest;
import com.cricket.scoring.dtos.PointsTableDTO;
import com.cricket.scoring.dtos.ResponseFiles.LeagueTable;
import com.cricket.scoring.dtos.TournamentTeamDTO;
import com.cricket.scoring.entities.PointsTable;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.Tournament;
import com.cricket.scoring.entities.TournamentTeam;
import com.cricket.scoring.entities.enums.TournamentStatus;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.repositories.TournamentRepository;
import com.cricket.scoring.repositories.TournamentTeamRepository;
import com.cricket.scoring.services.JsonFileService;
import com.cricket.scoring.services.PointsTableService;
import com.cricket.scoring.services.TournamentService;
import com.cricket.scoring.services.TournamentTeamService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final ModelMapper modelMapper;
    private final PointsTableService pointsTableService;
    private final TournamentTeamRepository tournamentTeamRepository;

    @Override
    public List<CreateTournamentResponse> getAllTournaments() {
        List<CreateTournamentResponse> responses = tournamentRepository.findAll()
                .stream()
                .map(tournament ->
                        modelMapper.map(
                                tournament,
                                CreateTournamentResponse.class
                        )
                )
                .toList();
        return responses;
    }

    @Override
    public CreateTournamentResponse getTournamentById(Long id) {
        Tournament tournament = tournamentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id "+ id));
        return modelMapper.map(tournament, CreateTournamentResponse.class);
    }

    @Override
    public CreateTournamentResponse createTournament(CreateTournametRequest request) {
        Tournament tournament = modelMapper.map(request, Tournament.class);
        tournament.setStatus(TournamentStatus.UPCOMING);
        tournament.setCreatedAt(LocalDateTime.now());
        tournament.setUpdatedAt(LocalDateTime.now());
        Tournament savedTournament = tournamentRepository.save(tournament);
        CreateTournamentResponse response = modelMapper.map(savedTournament, CreateTournamentResponse.class);

        pointsTableService.generatePointsTable(savedTournament.getId());
        return response;
    }

    @Override
    public void deleteTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Tournament not found with id "+id));
        tournamentRepository.delete(tournament);
    }
}
