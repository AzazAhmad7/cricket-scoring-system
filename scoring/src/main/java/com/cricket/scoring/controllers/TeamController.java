package com.cricket.scoring.controllers;

import com.cricket.scoring.dtos.TeamDTO;
import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.services.PlayerService;
import com.cricket.scoring.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {
    private final TeamService teamService;
    private final PlayerService playerService;

    @PostMapping("/create")
    public ResponseEntity<TeamDTO> addTeam(@RequestBody TeamDTO teamDTO) {
        return ResponseEntity.ok(teamService.createTeam(teamDTO));
    }

    @GetMapping
    public ResponseEntity<List<TeamDTO>> getAllTeams(){
        return ResponseEntity.ok(teamService.getAllTeams());
    }
    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Long id){
        return ResponseEntity.ok(teamService.getTeam(id));
    }

    @DeleteMapping("/{id}")
    public void deleteTeamById(@PathVariable Long id){
        teamService.deleteTeam(id);
    }

    @GetMapping("/{teamId}/players")
    public ResponseEntity<List<Player>> getPlayerByTeam(@PathVariable Long teamId){
        return ResponseEntity.ok(playerService.getPlayersByTeam(teamId));
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<TeamDTO> updateTeam(@PathVariable Long teamId, @RequestBody TeamDTO teamDTO){
        return ResponseEntity.ok(teamService.updateTeam(teamId, teamDTO));
    }
}
