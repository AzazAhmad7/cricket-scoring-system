package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.PointsTableDTO;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.entities.Match;
import com.cricket.scoring.entities.Tournament;
import com.cricket.scoring.entities.enums.DismissalType;
import com.cricket.scoring.entities.enums.EventType;
import com.cricket.scoring.entities.enums.ExtraType;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.services.JsonFileService;
import com.cricket.scoring.services.MatchService;
import com.cricket.scoring.utils.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JsonFileServiceImpl implements JsonFileService {

    private final Map<Long, MatchAllData> matchAllDataMap = new ConcurrentHashMap<>();

    @Override
    public void createSetupFile(SetupFile setupFile) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            String fileName = "match_" + setupFile.getMatchInfo().getMatchId() + ".json";

            File dir = new File("C:/scoring/match/setups");         // saves to c/scoring/matches/ folder
            if (!dir.exists()) dir.mkdirs();

            objectMapper.writeValue(new File(dir, fileName), setupFile);

        } catch (IOException e) {
            // log but don't fail the request — file write is non-critical
            System.err.println("Failed to write match JSON: " + e.getMessage());
            throw new RuntimeException("Failed to write match JSON: " + e.getMessage());
        }
    }
    @Override
    public void createPointsTable(Tournament tournament, LeagueTable leagueTable) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            String fileName = tournament.getName()+" pointsTable.json";

            File dir = new File("C:/scoring/match/pointsTables");         // saves to c/scoring/matches/ folder
            if (!dir.exists()) dir.mkdirs();

            objectMapper.writeValue(new File(dir, fileName), leagueTable);

        } catch (IOException e) {
            // log but don't fail the request — file write is non-critical
            System.err.println("Failed to write match JSON: " + e.getMessage());
            throw new RuntimeException("Failed to write match JSON: " + e.getMessage());
        }
    }

    @Override
    public void createMatchStateFile(MatchState matchState) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            String fileName = "match_" + matchState.getMatchId() + ".json";

            File dir = new File("C:/scoring/match/matchStates");         // saves to c/scoring/matches/ folder
            if (!dir.exists()) dir.mkdirs();

            objectMapper.writeValue(new File(dir, fileName), matchState);

        } catch (IOException e) {
            // log but don't fail the request — file write is non-critical
            System.err.println("Failed to write match JSON: " + e.getMessage());
            throw new RuntimeException("Failed to write match JSON: " + e.getMessage());
        }
    }

    @Override
    public void createEventFile(MatchState matchState,EventFile eventFile) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            String fileName = "match_" + matchState.getMatchId() + ".json";

            File dir = new File("C:/scoring/match/events");         // saves to c/scoring/matches/ folder
            if (!dir.exists()) dir.mkdirs();

            objectMapper.writeValue(new File(dir, fileName), eventFile);

        } catch (IOException e) {
            // log but don't fail the request — file write is non-critical
            System.err.println("Failed to write match JSON: " + e.getMessage());
            throw new RuntimeException("Failed to write match JSON: " + e.getMessage());
        }
    }

    @Override
    public List<Event> appendEvent(MatchState matchState, Inning inning, Event event, EventFile eventFile) {
        List<Event> events = eventFile.getEvents();

        Integer runsOfBat = null;
        boolean isLegalDelivery = true;
        boolean isFreeHit = false;
        boolean isWicket = false;
        Long dismissedPlayerId = null;
        Integer eventNumber = null;
        ExtraType extraType = null;
        Integer extraRuns = null;
        ExtraType subExtraType = null;
        Integer subExtraRuns = null;
        Long currentBowlerId = inning.getCurrentBowlerId();

        switch (event.getEventType()) {
            case DOT_BALL -> runsOfBat = 0;
            case ONE -> runsOfBat = 1;
            case TWO -> runsOfBat = 2;
            case THREE -> runsOfBat = 3;
            case FOUR -> runsOfBat = 4;
            case FIVE -> runsOfBat = 5;
            case SIX -> runsOfBat = 6;
            case WICKET -> {
                if(event.getDismissedType() != DismissalType.RETIRED_HURT){
                    isWicket = true;
                }
                dismissedPlayerId = event.getDismissedPlayerId();
            }
            case WIDE -> {
                isLegalDelivery = false;
                extraType = ExtraType.WIDE;
                extraRuns = 1;
            }
            case NO_BALL -> {
                isLegalDelivery = false;
                isFreeHit = true;
                extraType = ExtraType.NO_BALL;
                extraRuns = 1;
            }
            case BYE -> {
                subExtraRuns = 1;
                extraType = ExtraType.BYE;
            }
            case LEG_BYE -> {
                subExtraRuns = 1;
                extraType = ExtraType.LEG_BYE;
            }
            case ANY_BALL -> {
                if(event.getIsWideAnyBall()){
                    isLegalDelivery = false;
                    extraType = ExtraType.WIDE;
                    extraRuns = 1;
                    if(event.getIsByeAnyBall()){
                        subExtraType = ExtraType.BYE;
                    }else if(event.getIsLegByeAnyBall()){
                        subExtraType = ExtraType.LEG_BYE;
                    }
                    runsOfBat =event.getRunsOffBatAnyBall() == null ? 0 : event.getRunsOffBatAnyBall();
                    subExtraRuns = event.getRunsOfByeAnyBall();
                    if(event.getIsWicket()){
                        if(event.getDismissedType() != DismissalType.RETIRED_HURT){
                            isWicket = true;
                        }
                        dismissedPlayerId = event.getDismissedPlayerId();
                    }
                }else if(event.getIsNoBallAnyBall()){
                    isLegalDelivery = false;
                    extraType = ExtraType.NO_BALL;
                    extraRuns = 1;
                    if(event.getIsByeAnyBall()){
                        subExtraType = ExtraType.BYE;
                    }else if(event.getIsLegByeAnyBall()){
                        subExtraType = ExtraType.LEG_BYE;
                    }
                    runsOfBat =event.getRunsOffBatAnyBall() == null ? 0 : event.getRunsOffBatAnyBall();
                    subExtraRuns = event.getRunsOfByeAnyBall();
                    if(event.getIsWicket()){
                        if(event.getDismissedType() != DismissalType.RETIRED_HURT){
                            isWicket = true;
                        }
                        dismissedPlayerId = event.getDismissedPlayerId();
                    }
                }else if(event.getIsByeAnyBall()){
                    extraType = ExtraType.BYE;
                    extraRuns = 0;
                    subExtraType = ExtraType.BYE;
                    subExtraRuns = event.getRunsOfByeAnyBall();
                    if(event.getIsWicket()){
                        if(event.getDismissedType() != DismissalType.RETIRED_HURT){
                            isWicket = true;
                        }
                        dismissedPlayerId = event.getDismissedPlayerId();
                    }
                }else if(event.getIsLegByeAnyBall()){
                    extraType = ExtraType.LEG_BYE;
                    extraRuns = 0;
                    subExtraType = ExtraType.LEG_BYE;
                    subExtraRuns = event.getRunsOfByeAnyBall();
                    if(event.getIsWicket()){
                        if(event.getDismissedType() != DismissalType.RETIRED_HURT){
                            isWicket = true;
                        }
                        dismissedPlayerId = event.getDismissedPlayerId();
                    }
                }
            }
            case END_OVER -> {

                inning.setCurrentBowlerId(null);

                BowlerCard boc = matchState.getScoreCard()
                        .getInnings()
                        .get(matchState.getCurrentInningNumber() - 1)
                        .getBowlingCard()
                        .getBowlers()
                        .stream()
                        .filter(bc ->
                                Boolean.TRUE.equals(bc.getIsCurrentBowler())
                                        &&
                                        Objects.equals(
                                                bc.getBowler().getPlayerId(),
                                                currentBowlerId
                                        )
                        )
                        .findAny()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Current bowler not found"
                                ));

                boc.setIsCurrentBowler(false);
            }
        }
        if(eventFile.getEvents().isEmpty()){
            eventNumber = 1;
        }else{
            eventNumber = eventFile.getEvents().size()+1;
        }

        Integer totalLegalDeliveries = eventFile.getEvents().isEmpty() ? 0 : eventFile.getEvents().getLast().getTotalLegalBallsBowled();

        Event newEvent = Event.builder()
                .eventNumber(eventNumber)
                .inningNumber(matchState.getCurrentInningNumber())
                .matchId(matchState.getMatchId())
                .eventType(event.getEventType())
                .oversCompleted(inning.getScoreSummary().getOvers())
                .ballInOver(inning.getScoreSummary().getBalls())
                .totalLegalBallsBowled(totalLegalDeliveries)
                .strikerId(inning.getStrikerId())
                .nonStrikerId(inning.getNonStrikerId())
                .bowlerId(event.getEventType() == EventType.END_OVER ? currentBowlerId : inning.getCurrentBowlerId())
                .runOffBat(runsOfBat)
                .extrasRuns(extraRuns)
                .subExtrasRuns(subExtraRuns)
                .extraType(extraType)
                .subExtraType(subExtraType)
                .isLegalDelivery(isLegalDelivery)
                .isFreeHit(isFreeHit)
                .isWicket(isWicket)
                .dismissedPlayerId(dismissedPlayerId)
                .fielderId(event.getFielderId())
                .dismissedType(event.getDismissedType())
                .dismissalText(event.getDismissalText())
                .ballOutCome(event.getBallOutCome())
                .playerId(event.getPlayerId())
                .isImpact(event.getIsImpact())
                .impactInPlayerId(event.getImpactInPlayerId())
                .impactOutPlayerId(event.getImpactOutPlayerId())
                .teamId(event.getTeamId())
                .build();
        events.add(newEvent);
        return events;
    }

    @Override
    public SetupFile loadSetupFile(Long matchId) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            File file =
                    new File("C:/scoring/match/setups/match_" + matchId + ".json"
                    );

            return objectMapper.readValue(file,SetupFile.class
            );
        }catch (Exception e){
            System.err.println("Failed to load match setup JSON: " + e.getMessage());
            throw new RuntimeException("Failed to load match setup JSON: " + e.getMessage());
        }
    }

    @Override
    public MatchState loadStateFile(Long matchId) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("C:/scoring/match/matchStates/match_" + matchId + ".json");

            return objectMapper.readValue(file,MatchState.class);
        }catch (Exception e){
            System.err.println("Failed to load match State JSON: " + e.getMessage());
            throw new RuntimeException("Failed to load match state JSON: " + e.getMessage());
        }
    }

    @Override
    public LeagueTable loadLeagueTable(String tournamentName) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("C:/scoring/match/pointsTables/" + tournamentName + " pointsTable.json");

            return objectMapper.readValue(file,LeagueTable.class);
        }catch (Exception e){
            System.err.println("Failed to load league table JSON: " + e.getMessage());
            throw new RuntimeException("Failed to load match state JSON: " + e.getMessage());
        }
    }

    @Override
    public EventFile loadEventFile(Long matchId) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("C:/scoring/match/events/match_" + matchId + ".json");

            return objectMapper.readValue(file,EventFile.class
            );
        }catch (Exception e){
            System.err.println("Failed to load match State JSON: " + e.getMessage());
            throw new RuntimeException("Failed to load match state JSON: " + e.getMessage());
        }
    }

    @Override
    public void updateStateFile(MatchState matchState) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.registerModule(new JavaTimeModule());

            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            String fileName = "match_" + matchState.getMatchId() + ".json";

            File dir = new File( "C:/scoring/match/matchStates");

            if(!dir.exists()){dir.mkdirs();}

            objectMapper.writeValue(new File(dir,fileName),matchState);

        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    public void updateSetUpFile(SetupFile setupFile) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.registerModule(new JavaTimeModule());

            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            String fileName = "match_" + setupFile.getMatchInfo().getMatchId() + ".json";

            File dir = new File( "C:/scoring/match/setups");

            if(!dir.exists()){dir.mkdirs();}

            objectMapper.writeValue(new File(dir,fileName),setupFile);

        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    public void clearEventFile(Long matchId) {

        try {

            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.registerModule(new JavaTimeModule());

            objectMapper.disable(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
            );

            objectMapper.enable(
                    SerializationFeature.INDENT_OUTPUT
            );

            String fileName = "match_" + matchId + ".json";

            File dir = new File("C:/scoring/match/events");
            File file = new File(dir, fileName);

            if(!dir.exists()){
                dir.mkdirs();
            }
            EventFile eventFile = new EventFile();

            objectMapper.writeValue(file,eventFile);
            System.out.println("Cleared file: " + file.getAbsolutePath());
            System.out.println(objectMapper.writeValueAsString(eventFile));
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void rebuildStateFromEvents() {

    }

    @Override
    public MatchAllData loadMatchAllData(Long matchId) {
        SetupFile loadedSetup = loadSetupFile(matchId);
        MatchState loadedMatch = loadStateFile(matchId);
        EventFile loadedEvents = loadEventFile(matchId);

        MatchAllData matchAllData = MatchAllData.builder()
                .setupFile(loadedSetup)
                .matchState(loadedMatch)
                .eventFile(loadedEvents)
                .build();
        return matchAllData;
    }

    public MatchAllData loadMatch(Long matchId) {

        return matchAllDataMap.computeIfAbsent(matchId, id -> {
            MatchState state = loadStateFile(id);
            SetupFile setup = loadSetupFile(id);
            EventFile event = loadEventFile(id);
            MatchAllData data = MatchAllData.builder()
                    .matchState(state)
                    .setupFile(setup)
                    .eventFile(event)
                    .build();

            System.out.println("data = " + data);

            return data;
        });
    }
    public MatchAllData initializeMatch(MatchState matchState, SetupFile setupFile, EventFile eventFile, Long matchId) {

        MatchAllData data = MatchAllData.builder()
                .matchState(matchState)
                .setupFile(setupFile)
                .eventFile(eventFile)
                .build();

        matchAllDataMap.put(matchId, data);
        return data;
    }

    @Override
    public Boolean mapContains(Long matchId) {
        return matchAllDataMap.containsKey(matchId);
    }
}
