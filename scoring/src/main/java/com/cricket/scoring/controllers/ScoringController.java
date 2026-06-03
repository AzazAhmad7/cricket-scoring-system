package com.cricket.scoring.controllers;

import com.cricket.scoring.dtos.ImpactPlayerDTO;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.services.ScoringService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
public class ScoringController {
    private final ScoringService scoringService;

    @PostMapping("/scoreBall")
    public void scoreBall(@RequestBody Event event){
        scoringService.scoreBall(event);
    }

    @PostMapping("/impactPlayer/{matchId}")
    public void impactPlayer(@RequestBody ImpactPlayerDTO impactPlayerDTO, @PathVariable Long matchId){
        scoringService.impactPlayer(impactPlayerDTO, matchId);
    }

    @PostMapping("/selectBatter/{playerId}")
    public ResponseEntity<BatterCard> selectNewBatter(@PathVariable Long playerId, @RequestBody Event event){
        System.out.println("PlayerId "+playerId+" Event "+event);
        return ResponseEntity.ok(scoringService.selectNewBatter(playerId, event));
    }

    @PostMapping("/selectBowler/{playerId}")
    public ResponseEntity<BowlingCard> selectBowler(@PathVariable Long playerId, @RequestBody Event event){
        return ResponseEntity.ok(scoringService.selectNewBowler(playerId, event));
    }

    @PostMapping("/{matchId}/rebuild")
    public ResponseEntity<MatchState> rebuildMatch(@PathVariable Long matchId){
        return ResponseEntity.ok(scoringService.rebuildMatchState(matchId));
    }

    @PostMapping("/endOver")
    public void endOver(@RequestBody Event event){
        scoringService.endOver(event);
    }

    @PostMapping("/swapStriker")
    public ResponseEntity<Boolean> swapStriker(@RequestBody MatchState matchState){
        return ResponseEntity.ok(scoringService.swapStrikerDirectly(matchState));
    }
}
