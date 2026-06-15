package com.cricket.scoring.controllers;

import com.cricket.scoring.dtos.AddTeamsToTournamentRequest;
import com.cricket.scoring.dtos.CreateTournamentResponse;
import com.cricket.scoring.dtos.CreateTournametRequest;
import com.cricket.scoring.dtos.TournamentTeamDTO;
import com.cricket.scoring.services.TournamentService;
import com.cricket.scoring.services.TournamentTeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentTeamService tournamentTeamService;

    @GetMapping
    public ResponseEntity<List<CreateTournamentResponse>> getAllTournaments(){
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }
    @GetMapping("/{tournamentId}")
    public ResponseEntity<CreateTournamentResponse> getTournamentById(@PathVariable Long tournamentId){
        return ResponseEntity.ok(tournamentService.getTournamentById(tournamentId));
    }
    @PostMapping("/create")
    public ResponseEntity<CreateTournamentResponse> createTournament(@RequestBody CreateTournametRequest request){
        return new ResponseEntity<>(tournamentService.createTournament(request), HttpStatus.CREATED);
    }
    @PostMapping("/teams")
    public ResponseEntity<Void> addTeamsToTournament(@RequestBody AddTeamsToTournamentRequest request) {
        tournamentTeamService.addTeams(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{tournamentId}")
    public void deleteTournament(@PathVariable Long tournamentId){
        tournamentService.deleteTournament(tournamentId);
    }
}
