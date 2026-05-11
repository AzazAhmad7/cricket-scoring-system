package com.cricket.scoring.controllers;

import com.cricket.scoring.dtos.PlayerDTO;
import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.repositories.PlayerRepository;
import com.cricket.scoring.services.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/players")
public class PlayerController {
    private final PlayerService playerService;

    @PostMapping("/create")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        return new ResponseEntity<>( playerService.createPlayer(player), HttpStatus.CREATED);
    }

    @PostMapping("/createBulk")
    public void createBulkPlayers(@RequestBody List<Player> players) {
        playerService.createPlayers(players);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
    }


    @PatchMapping("/{playerId}/teams/{teamId}")
    public ResponseEntity<Player> assignTeamToPlayer(@PathVariable Long playerId,@PathVariable Long teamId) {
        return ResponseEntity.ok(playerService.assignPlayerToTeam(playerId, teamId));
    }

    @PatchMapping("/{playerId}/remove")
    public ResponseEntity<Player> removePlayerFromTeam(@PathVariable Long playerId){
        return ResponseEntity.ok(playerService.removePlayerFromTeam(playerId));
    }
}
