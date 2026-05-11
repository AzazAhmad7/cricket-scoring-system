package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.ResponseFiles.*;
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
    public void appendEvent(MatchState matchState, Inning inning, Event event, EventFile eventFile) {
        List<Event> events = eventFile.getEvents();
        System.out.println(event);

        Integer runsOfBat = null;
        boolean isLegalDelivery = true;
        boolean isFreeHit = false;
        boolean isWicket = false;
        Long dismissedPlayerId = null;
        Integer eventNumber = null;

        switch (event.getEventType()) {
            case DOT_BALL -> runsOfBat = 0;
            case ONE -> runsOfBat = 1;
            case TWO -> runsOfBat = 2;
            case THREE -> runsOfBat = 3;
            case FOUR -> runsOfBat = 4;
            case FIVE -> runsOfBat = 5;
            case SIX -> runsOfBat = 6;
            case WICKET -> {
                isWicket = true;
                dismissedPlayerId = matchState.getStrikerId();
            }
            case WIDE -> isLegalDelivery = false;
            case NO_BALL -> {
                isLegalDelivery = false;
                isFreeHit = true;
            }
            case END_OVER -> {
                matchState.setCurrentBowlerId(null);
                BowlerCard boc = matchState.getScoreCard().getInnings().get(matchState.getCurrentInningNumber()-1).getBowlingCard().getBowlers().stream()
                        .filter(bc -> bc.getIsCurrentBowler() && !Objects.equals(bc.getBowler().getPlayerId(), matchState.getCurrentBowlerId()))
                        .findAny().orElseThrow(()-> new ResourceNotFoundException("No duplicate bowlers found"));

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
                .strikerId(matchState.getStrikerId())
                .nonStrikerId(matchState.getNonStrikerId())
                .bowlerId(matchState.getCurrentBowlerId())
                .runOffBat(runsOfBat)
                .extrasRuns(event.getExtrasRuns())
                .subExtrasRuns(event.getSubExtrasRuns())
                .extraType(event.getExtraType())
                .subExtraType(event.getSubExtraType())
                .isLegalDelivery(isLegalDelivery)
                .isFreeHit(isFreeHit)
                .isWicket(isWicket)
                .dismissedPlayerId(dismissedPlayerId)
                .fielderId(event.getFielderId())
                .dismissedType(event.getDismissedType())
                .dismissalText(event.getDismissalText())
                .ballOutCome(event.getBallOutCome())
                .build();
        events.add(newEvent);
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

            return objectMapper.readValue(file,MatchState.class
            );
        }catch (Exception e){
            System.err.println("Failed to load match State JSON: " + e.getMessage());
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
        matchAllDataMap.put(matchId, matchAllData);
        return matchAllData;
    }
    public MatchState getMatchStateFromMemory(Long matchId){
        return matchAllDataMap.get(matchId).getMatchState();
    }
    public SetupFile getSetupFileFromMemory(Long matchId){
        return matchAllDataMap.get(matchId).getSetupFile();
    }
    public EventFile getEventsFromMemory(Long matchId){
        return matchAllDataMap.get(matchId).getEventFile();
    }
}
