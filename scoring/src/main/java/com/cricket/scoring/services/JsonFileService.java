package com.cricket.scoring.services;

import com.cricket.scoring.dtos.ResponseFiles.*;

public interface JsonFileService {

    void createSetupFile(SetupFile setupFile);

    void createMatchStateFile(MatchState matchState);

    void createEventFile(MatchState matchState, EventFile eventFile);

    void appendEvent(MatchState matchState, Inning inning, Event event, EventFile eventFile);

    SetupFile loadSetupFile(Long matchId);

    MatchState loadStateFile(Long matchId);

    EventFile loadEventFile(Long matchId);

    void updateStateFile(MatchState matchState);
    void updateSetUpFile(SetupFile setupFile);

    void rebuildStateFromEvents();

    MatchAllData loadMatchAllData(Long matchId);
    MatchState getMatchStateFromMemory(Long matchId);
    SetupFile getSetupFileFromMemory(Long matchId);
    EventFile getEventsFromMemory(Long matchId);
}
