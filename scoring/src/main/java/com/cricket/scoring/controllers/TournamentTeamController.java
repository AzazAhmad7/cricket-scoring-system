package com.cricket.scoring.controllers;

import com.cricket.scoring.dtos.TournamentTeamDTO;
import com.cricket.scoring.entities.TournamentTeam;
import com.cricket.scoring.services.TournamentTeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tournamentTeams")
@RequiredArgsConstructor
public class TournamentTeamController {
    private final TournamentTeamService tournamentTeamService;

    @GetMapping("{tournamentId}")
    public ResponseEntity<List<TournamentTeamDTO>> getAllTeamsOfTournament(@PathVariable Long tournamentId){
        return ResponseEntity.ok(tournamentTeamService.getAllTeamsOfTournament(tournamentId));
    }

//    @GetMapping("/{tournamentId}")
//    public ResponseEntity<TournamentTeamDTO> getTournamentTeamById(@PathVariable Long tournamentId){
//        return ResponseEntity.ok(tournamentTeamService.getTournamentTeamById(tournamentId));
//    }
}
