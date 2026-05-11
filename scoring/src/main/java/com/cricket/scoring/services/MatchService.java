package com.cricket.scoring.services;

import com.cricket.scoring.dtos.MatchStateDTO;
import com.cricket.scoring.dtos.*;
import com.cricket.scoring.dtos.ResponseFiles.MatchAllData;
import com.cricket.scoring.dtos.ResponseFiles.MatchState;
import com.cricket.scoring.dtos.ResponseFiles.ScoreCard;
import com.cricket.scoring.dtos.ResponseFiles.SetupFile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MatchService {

    MatchAllData createMatch(CreateMatchRequest request);

    MatchAllData getMatchAllData(Long matchId);

    MatchState startMatch(SetupFile setupFile);

    MatchDTO getMatch(Long matchId);

    List<Long> getAllMatches();

    SetupFile updateMatch(Long matchId, CreateMatchRequest request);

    void deleteMatch(Long matchId);

    Boolean matchExists(Long matchId);

    void assignSquads();

}
