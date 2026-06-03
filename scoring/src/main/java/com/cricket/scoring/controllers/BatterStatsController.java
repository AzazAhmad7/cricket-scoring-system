package com.cricket.scoring.controllers;

import com.cricket.scoring.dtos.BatterStatsDTO;
import com.cricket.scoring.services.BatterStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/batterStats")
@RequiredArgsConstructor
public class BatterStatsController {
    private final BatterStatsService batterStatsService;

    @GetMapping("/tournament/{tournamentId}/player/{playerId}")
    public ResponseEntity<BatterStatsDTO> getBatterStatsOfPlayer(@PathVariable Long tournamentId, @PathVariable Long playerId){
        BatterStatsDTO batterStatsDTO = batterStatsService.getBatterStatsOfPlayer(playerId, tournamentId);
        return ResponseEntity.ok(batterStatsDTO);
    }
}
