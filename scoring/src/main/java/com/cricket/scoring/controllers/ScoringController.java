package com.cricket.scoring.controllers;

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
        System.out.println("EVENT" +event);
        scoringService.scoreBall(event);
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

    @PostMapping("/endOver")
    public void endOver(@RequestBody Event event){
        scoringService.endOver(event);
    }
}
