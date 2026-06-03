package com.cricket.scoring.services;

import com.cricket.scoring.dtos.CreateTournamentResponse;
import com.cricket.scoring.dtos.CreateTournametRequest;

import java.util.List;

public interface TournamentService {
    List<CreateTournamentResponse> getAllTournaments();
    CreateTournamentResponse getTournamentById(Long id);
    CreateTournamentResponse createTournament(CreateTournametRequest request);
}
