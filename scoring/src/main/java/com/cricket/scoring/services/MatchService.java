package com.cricket.scoring.services;

import com.cricket.scoring.dtos.MatchStateDTO;
import com.cricket.scoring.dtos.*;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.entities.enums.MatchStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface MatchService {

    MatchAllData createMatch(CreateMatchRequest request);

    MatchAllData updateMatchStatus(Long matchId, MatchStatus matchStatus);

    MatchAllData changeInning(Long matchId);

    MatchAllData getMatchAllData(Long matchId);

    MatchState startMatch(SetupFile setupFile, MatchState matchState, Integer inningNumber, MatchStatus matchStatus);

    SetupFile endMatch(MatchState matchState, SetupFile setupFile);

    MatchDTO getMatch(Long matchId);

    List<MatchDTO> getAllMatches();

    SetupFile updateMatch(Long matchId, CreateMatchRequest request);

    void deleteMatch(Long matchId);

    Boolean matchExists(Long matchId);

    void assignSquads();

    SetupFile getMatchSetup(Long matchId);

    void resetMatch(Long matchId);
    MatchState createFreshMatchState(Long matchId);
}
