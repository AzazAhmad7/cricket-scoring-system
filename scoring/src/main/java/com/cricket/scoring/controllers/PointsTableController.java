package com.cricket.scoring.controllers;

import com.cricket.scoring.entities.Tournament;
import com.cricket.scoring.services.PointsTableService;
import com.cricket.scoring.services.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pointsTable")
@RequiredArgsConstructor
public class PointsTableController {

    private final PointsTableService pointsTableService;

    @PostMapping("/{tournamentId}/generate-points-table")
    public ResponseEntity<Void> generatePointsTable(@PathVariable Long tournamentId) {
        pointsTableService.generatePointsTable(tournamentId);
        return ResponseEntity.ok().build();
    }
}
