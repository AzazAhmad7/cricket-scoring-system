package com.cricket.scoring.services;

import com.cricket.scoring.dtos.PointsTableDTO;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.entities.Tournament;

import java.util.List;
import java.util.Set;

public interface JsonFileService {

    void createSetupFile(SetupFile setupFile);

    void createMatchStateFile(MatchState matchState);

    void createEventFile(MatchState matchState, EventFile eventFile);

    List<Event> appendEvent(MatchState matchState, Inning inning, Event event, EventFile eventFile);

    SetupFile loadSetupFile(Long matchId);

    MatchState loadStateFile(Long matchId);

    EventFile loadEventFile(Long matchId);

    void updateStateFile(MatchState matchState);
    void updateSetUpFile(SetupFile setupFile);
    void clearEventFile(Long matchId);

    void rebuildStateFromEvents();

    MatchAllData loadMatchAllData(Long matchId);
    MatchAllData loadMatch(Long matchId);
    MatchAllData initializeMatch(MatchState matchState, SetupFile setupFile, EventFile eventFile, Long matchId);
    Boolean mapContains(Long matchId);
    void createPointsTable(Tournament tournament, LeagueTable leagueTable);
    LeagueTable loadLeagueTable(String tournamentName);
}
