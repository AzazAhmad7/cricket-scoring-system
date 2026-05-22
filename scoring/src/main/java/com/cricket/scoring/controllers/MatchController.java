package com.cricket.scoring.controllers;

import com.cricket.scoring.dtos.*;
import com.cricket.scoring.dtos.ResponseFiles.MatchAllData;
import com.cricket.scoring.dtos.ResponseFiles.SetupFile;
import com.cricket.scoring.entities.enums.MatchStatus;
import com.cricket.scoring.services.JsonFileService;
import com.cricket.scoring.services.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
public class MatchController {
    private final MatchService matchService;
    private final JsonFileService jsonFileService;

    @PostMapping("/create")
    public ResponseEntity<MatchAllData> createMatch(@RequestBody CreateMatchRequest request){
        return new ResponseEntity<>(matchService.createMatch(request), HttpStatus.CREATED) ;
    }
    @PostMapping("/{matchId}/innings")
    public ResponseEntity<MatchAllData> changeInning(@PathVariable Long matchId){
        return new ResponseEntity<>(matchService.changeInning(matchId), HttpStatus.OK);
    }

    @PatchMapping("/{matchId}")
    public ResponseEntity<SetupFile> updateMatch(@PathVariable Long matchId,@RequestBody CreateMatchRequest request){
        return ResponseEntity.ok(matchService.updateMatch(matchId, request));
    }

    @PutMapping("/{matchId}/status")
    public ResponseEntity<MatchAllData> updateMatchStatus(@PathVariable Long matchId, @RequestBody UpdateMatchStatusDTO updateMatchStatusDTO){
        return ResponseEntity.ok(matchService.updateMatchStatus(matchId, updateMatchStatusDTO.getMatchStatus()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getMatchById(@PathVariable Long id){
        return ResponseEntity.ok(matchService.getMatch(id));
    }

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAllMatches(){
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @DeleteMapping("/{id}")
    public void deleteMatchById(@PathVariable Long id){
        matchService.deleteMatch(id);
    }

    @GetMapping("{matchId}/matchAllData")
    public ResponseEntity<MatchAllData> getMatchAllData(@PathVariable Long matchId){
        return ResponseEntity.ok(matchService.getMatchAllData(matchId));
    }
}
