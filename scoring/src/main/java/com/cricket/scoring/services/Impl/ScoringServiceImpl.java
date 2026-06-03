package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.*;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.entities.Match;
import com.cricket.scoring.entities.Tournament;
import com.cricket.scoring.entities.enums.*;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.exceptions.RuntimeConflictException;
import com.cricket.scoring.repositories.PlayerRepository;
import com.cricket.scoring.repositories.TournamentRepository;
import com.cricket.scoring.services.*;
import com.cricket.scoring.utils.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cricket.scoring.entities.enums.DismissalType.RETIRED_HURT;
import static com.cricket.scoring.entities.enums.DismissalType.RETIRED_OUT;
import static com.cricket.scoring.entities.enums.EventType.*;


@Service
@RequiredArgsConstructor
public class ScoringServiceImpl implements ScoringService {

    private final JsonFileService jsonFileService;
    private final ScoreCardService scoreCardService;
    private final MatchService matchService;
    private final PointsTableService pointsTableService;
    private final TournamentService tournamentService;


    //CHANGING SCOREBALL METHOD
    @Override
    public void scoreBall(Event event) {

        if(event.getEventType() == EventType.UNDO){
            undoLastBall(event.getMatchId());
            return;
        }
        MatchAllData matchAllData = jsonFileService.loadMatch(event.getMatchId());

        MatchState matchState = matchAllData.getMatchState();

        SetupFile setupFile = matchAllData.getSetupFile();

        EventFile eventFile = matchAllData.getEventFile();

        validateMatchState(matchState, setupFile, eventFile);

        processAndPersistEvent( matchState, setupFile, eventFile, event );
    }
    private void processAndPersistEvent( MatchState matchState, SetupFile setupFile, EventFile eventFile, Event event) {

        Inning inning = Util.getCurrentInning(matchState);

        jsonFileService.appendEvent(matchState, inning, event, eventFile);

        Event newEvent = eventFile.getEvents().getLast();

        applyEvent(matchState, inning, setupFile, newEvent, false);

        jsonFileService.updateStateFile(matchState);

        jsonFileService.createEventFile(matchState, eventFile);
    }
    private void applyEvent(MatchState matchState, Inning inning, SetupFile setupFile, Event event, boolean isRebuilding) {
        updateBallCount(event, inning);
        updateTeamScore(event, matchState, inning);
        scoreCardService.updateBattingCard(matchState, inning, event);
        scoreCardService.updatePartnership(matchState, inning, event);
        scoreCardService.updateBowlingCard(matchState, inning, event);
        scoreCardService.updateOverProgression(matchState, inning, event);
        scoreCardService.updatePhaseBreakDown(matchState, inning, event);
        scoreCardService.updateInningControlMetrics(matchState, inning, event);

        handleStrikeRotation(matchState, inning, event);

        handleWicket(matchState, inning, event);

        handleEndOver(matchState, inning, event);

        handleTarget(matchState, inning, setupFile, event);

        handleMatchResult(matchState, inning, setupFile, event, isRebuilding);
    }
    private void undoLastBall(Long matchId) {
        MatchAllData matchAllData = jsonFileService.loadMatch(matchId);
        EventFile eventFile = matchAllData.getEventFile();

        if(eventFile.getEvents().isEmpty()){
            throw new RuntimeException("No ball to undo");
        }

        // remove last event
        eventFile.getEvents().removeLast();

        // rebuild state
        MatchState rebuiltState = rebuildMatchState(matchId);

        matchAllData.setMatchState(rebuiltState);
        jsonFileService.updateStateFile(rebuiltState);

        jsonFileService.createEventFile(rebuiltState, eventFile);
    }
    public MatchState rebuildMatchState(Long matchId) {
        MatchState freshState = matchService.createFreshMatchState(matchId);
        jsonFileService.createMatchStateFile(freshState);
        MatchAllData matchAllData = jsonFileService.loadMatch(matchId);
        EventFile eventFile = matchAllData.getEventFile();

        if (eventFile == null || eventFile.getEvents() == null) {
            return freshState;
        }

        SetupFile setupFile = matchAllData.getSetupFile();

        for(Event event : eventFile.getEvents()) {
            if(event.getEventType()==EventType.END_OVER){
                continue;
            }
            if(event.getInningNumber() > 1){
                freshState.setCurrentInningNumber(event.getInningNumber());
                Long battingTeamId = setupFile.getToss().getBowlingTeamId();
                freshState.setBattingTeamId(battingTeamId);
                matchAllData.setMatchState(freshState);
            }

            Inning inning = Util.getCurrentInning(freshState);
            if (inning == null) {
                throw new RuntimeException("Current inning is null while rebuilding match");
            }
            if(event.getEventType() == NEW_BATTER){
                if(inning.getStrikerId() == null || inning.getNonStrikerId() == null){
                    applyNewBatterForMatchBuilding(event.getPlayerId(), freshState, event, matchAllData);
                }
            }else if(event.getEventType().equals(BOWLER_CHANGE)){
                applyNewBowlerForMatchBuilding(event.getPlayerId(), freshState, event, matchAllData);
            }else if(event.getEventType().equals(IMPACT)){
                impactPlayerForMatchRebuilding(setupFile, freshState, eventFile, event, matchId);
            }else{
                applyEvent(freshState, inning, setupFile,event, true);
            }
        }

        jsonFileService.createSetupFile(setupFile);
        jsonFileService.createEventFile(freshState,eventFile);
        jsonFileService.createMatchStateFile(freshState);
        return freshState;
    }
    private void validateMatchState( MatchState matchState,SetupFile setupFile,EventFile eventFile) {

        if(matchState.getMatchStatus() == MatchStatus.COMPLETED){
            throw new RuntimeConflictException(
                    "Match completed"
            );
        }
        if(matchState.getMatchStatus() != MatchStatus.LIVE){
            throw new RuntimeConflictException(
                    "Match not live"
            );
        }
        Inning inning = Util.getCurrentInning(matchState);

        int totalBalls = Util.overBallsToBalls(setupFile.getRules().getOvers(), 0);

        if(inning.getScoreSummary().getWickets() >= 10){
            throw new RuntimeConflictException("Inning ended");
        }

        Event lastEvent = eventFile.getEvents().isEmpty()? null : eventFile.getEvents().getLast();
        if(lastEvent != null && lastEvent.getInningNumber().equals(1)){
            if(lastEvent.getTotalLegalBallsBowled() >= totalBalls){
                throw new RuntimeConflictException("Inning ended");
            }
        }else{
            if(Util.overBallsToBalls(inning.getScoreSummary().getOvers(), inning.getScoreSummary().getBalls()) >= totalBalls){
                throw new RuntimeConflictException("Inning ended");
            }
        }

    }


    private void updateBallCount(Event event, Inning inning) {

        ScoreSummary summary = inning.getScoreSummary();

        int overs = summary.getOvers() == null ? 0 : summary.getOvers();

        int balls = summary.getBalls() == null ? 0 : summary.getBalls();

        boolean legalDelivery = event.getExtraType() != ExtraType.WIDE && event.getExtraType() != ExtraType.NO_BALL && event.getDismissedType() != RETIRED_OUT && event.getDismissedType() != RETIRED_HURT;

        if(legalDelivery){

            balls++;

            if(balls == 6){
                overs++;
                balls = 0;
            }

            summary.setOvers(overs);
            summary.setBalls(balls);
        }

        int totalBalls = Util.overBallsToBalls( summary.getOvers(), summary.getBalls() );

        event.setTotalLegalBallsBowled(totalBalls);
    }
    private void handleStrikeRotation(MatchState matchState,Inning inning, Event event) {

        boolean rotateStrike = false;

        // RUN OFF BAT
        if(event.getRunOffBat() != null && event.getRunOffBat() % 2 != 0){
            rotateStrike = true;
        }

        // BYE / LEG BYE
        if(event.getSubExtrasRuns() != null && (event.getExtraType() == ExtraType.BYE || event.getExtraType() == ExtraType.LEG_BYE) && event.getSubExtrasRuns() % 2 != 0){
            rotateStrike = true;
        }

        if(rotateStrike){
            switchStrike(matchState);
        }
    }
    private void handleWicket(MatchState matchState,Inning inning,Event event) {

        if(event.getDismissedType() == DismissalType.RETIRED_HURT){
            removeDismissedPlayerFromCrease(event.getDismissedPlayerId(), inning);
            return;
        }
        if(event.getIsWicket() == null || !event.getIsWicket()){
            return;
        }

        ScoreSummary summary = inning.getScoreSummary();

        int wickets = summary.getWickets() == null ? 0 : summary.getWickets();

        summary.setWickets(wickets + 1);

        scoreCardService.updateFOW(event.getDismissedPlayerId(),matchState,inning,event);
        removeDismissedPlayerFromCrease(event.getDismissedPlayerId(), inning);

    }
    private void removeDismissedPlayerFromCrease(Long playerId, Inning inning){
        if(playerId == null){
            return;
        }

        if(playerId.equals(inning.getStrikerId())){

            inning.setStrikerId(null);

        } else if(playerId.equals(inning.getNonStrikerId())){

            inning.setNonStrikerId(null);
        }
    }
    private void handleEndOver(MatchState matchState, Inning inning, Event event) {

        ScoreSummary summary = inning.getScoreSummary();

        int balls = Util.overBallsToBalls( summary.getOvers(), summary.getBalls());

        if(balls == 0){
            return;
        }
        if(event.getExtraType() == ExtraType.WIDE || event.getExtraType() == ExtraType.NO_BALL) return;

        // END OF OVER
        if(balls % 6 == 0){

            // SWITCH STRIKE AT OVER END
            switchStrike(matchState);

            // REMOVE CURRENT BOWLER FLAG
            BowlingCard bowlingCard = inning.getBowlingCard();

            if(bowlingCard != null && bowlingCard.getBowlers() != null){

                bowlingCard.getBowlers()
                        .forEach(b -> b.setIsCurrentBowler(false));
            }
        }
    }
    private void handleTarget(MatchState matchState, Inning inning, SetupFile setupFile, Event event) {

        // ONLY FIRST INNING
        if(inning.getInningNumber() != 1){
            return;
        }

        int totalBalls = Util.overBallsToBalls(setupFile.getRules().getOvers(),0);

        int currentBalls = event.getTotalLegalBallsBowled();

        boolean inningEnded = currentBalls >= totalBalls || inning.getScoreSummary().getWickets() >= 10;

        if(!inningEnded){
            return;
        }

        int targetRuns = inning.getScoreSummary().getRuns() + 1;

        TargetDTO targetDTO = TargetDTO.builder()
                .targetRuns(targetRuns)
                .runsRemaining(targetRuns)
                .totalBalls(totalBalls)
                .ballsRemaining(totalBalls)
                .requiredRunRate(
                        Util.requiredRunRate(
                                targetRuns,
                                totalBalls
                        )
                )
                .wicketsRemaining(10)
                .targetAchieved(false)
                .build();

        matchState.setTargetDTO(targetDTO);
    }
    private void handleMatchResult(MatchState matchState, Inning inning, SetupFile setupFile, Event event, Boolean isRebuilding) {

        // ONLY SECOND INNING
        if(inning.getInningNumber() != 2){
            return;
        }

        TargetDTO targetDTO = matchState.getTargetDTO();

        if(targetDTO == null){
            return;
        }

        int totalBalls = Util.overBallsToBalls(setupFile.getRules().getOvers(),0);

        int ballsBowled = event.getTotalLegalBallsBowled();

        int runs = inning.getScoreSummary().getRuns();

        int wickets = inning.getScoreSummary().getWickets();

        int target = targetDTO.getTargetRuns();

        boolean targetAchieved = runs >= target;

        boolean allOut = wickets >= 10;

        boolean oversCompleted = ballsBowled >= totalBalls;

        MatchResultDTO result = null;

        Team battingTeam = matchState.getBattingTeamId().equals(setupFile.getTeams().getHomeTeam().getId())
                        ? setupFile.getTeams().getHomeTeam()
                        : setupFile.getTeams().getAwayTeam();

        Team bowlingTeam = battingTeam.getId().equals(setupFile.getTeams().getHomeTeam().getId())
                        ? setupFile.getTeams().getAwayTeam()
                        : setupFile.getTeams().getHomeTeam();

        // CHASING TEAM WON
        if(targetAchieved){
            result = MatchResultDTO.builder()
                    .resultType(MatchResultType.WIN)
                    .winningTeamId(battingTeam.getId())
                    .winningTeamName(battingTeam.getName())
                    .marginType(ResultMarginType.WICKETS)
                    .marginValue(10 - wickets)
                    .summary( battingTeam.getName()+ " won by "+ (10 - wickets)+ " wickets")
                    .build();
        }
        // DEFENDING TEAM WON
        else if((allOut || oversCompleted) && runs < target - 1){

            result = MatchResultDTO.builder()
                    .resultType(MatchResultType.WIN)
                    .winningTeamId(bowlingTeam.getId())
                    .winningTeamName(bowlingTeam.getName())
                    .marginType(ResultMarginType.RUNS)
                    .marginValue(target - runs - 1)
                    .summary(bowlingTeam.getName()+ " won by "+ (target - runs - 1) + " runs")
                    .build();
        }
        // TIE
        else if((allOut || oversCompleted) && runs == target - 1){

            result = MatchResultDTO.builder()
                    .resultType(MatchResultType.TIE)
                    .summary("Match tied")
                    .build();
        }
        if(result == null) return;
        matchState.setMatchResultDTO(result);
        matchState.setMatchStatus(MatchStatus.COMPLETED);

        if(!isRebuilding){
            matchService.endMatch(matchState, setupFile);
            CreateTournamentResponse tournamentResponse = tournamentService.getTournamentById(setupFile.getMatchInfo().getCompetition());
            LeagueTable leagueTable = jsonFileService.loadLeagueTable(tournamentResponse.getName());
            pointsTableService.updateLeagueTable(leagueTable, matchState, setupFile);
        }
    }

    @Override
    public BatterCard selectNewBatter(Long playerId, Event event) {
        MatchAllData matchAllData = jsonFileService.loadMatch(event.getMatchId());
        MatchState matchState = matchAllData.getMatchState();
        EventFile eventFile = matchAllData.getEventFile();
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+"! Change it to live for scoring");
        Inning inning = Util.getCurrentInning(matchState);

        if(inning.getStrikerId() != null && inning.getNonStrikerId() != null)
            throw new RuntimeConflictException("Both the Players are not out");
        if(playerId.equals(inning.getStrikerId()) || playerId.equals(inning.getNonStrikerId())) {
            throw new RuntimeConflictException("Player already batting");
        }
        List<BatterCard> bcPlayers = inning.getBattingCard().getBatters();
        BatterCard batterCard = bcPlayers.stream().filter(plyr -> plyr.getBatter().getPlayerId().equals(playerId)).findAny().orElseThrow(()->new ResourceNotFoundException("Player not found in Battercard"));
        if(BattingStatus.OUT.equals(batterCard.getDismissal().getStatus())){
            throw new RuntimeConflictException("Batter already dismissed");
        }
        boolean returningFromRetiredHurt = BattingStatus.RETIRED_HURT.equals(batterCard.getDismissal().getStatus());
        if(returningFromRetiredHurt){
            batterCard.getDismissal().setStatus(BattingStatus.NOT_OUT);
            batterCard.getDismissal().setDismissalType(null);
            batterCard.getDismissal().setDismissalText(null);
        }
        //creating partnership
        scoreCardService.createPartnershipCard(playerId,matchState, inning,event);
        int nextBatPosition = inning.getNextBattingPosition() == null ? 1 : inning.getNextBattingPosition();

        if (inning.getStrikerId() == null) {
            inning.setStrikerId(playerId);
            batterCard.setOnStrike(true);
        } else if (inning.getNonStrikerId() == null) {
            inning.setNonStrikerId(playerId);
            batterCard.setOnStrike(false);
        }
        //UPDATING CONTEXT METRICS OF PLAYER
        String entry = String.valueOf(inning.getScoreSummary().getRuns()) +"-"
                + String.valueOf(inning.getScoreSummary().getWickets());
        scoreCardService.updateContextMetricsOfBatter(batterCard, event, entry, null);
        batterCard.getDismissal().setStatus(BattingStatus.NOT_OUT);
        if(batterCard.getBatter().getBattingPosition() == null){
            batterCard.getBatter().setBattingPosition(nextBatPosition);
        }
        if(!returningFromRetiredHurt){
            inning.setNextBattingPosition(nextBatPosition+1);
        }
        Util.sortBattingCardByPosition(inning.getBattingCard());
        jsonFileService.createMatchStateFile(matchState);
        jsonFileService.appendEvent(matchState, inning, event, eventFile);
        jsonFileService.createEventFile(matchState, eventFile);
        return batterCard;
    }
    public BatterCard applyNewBatterForMatchBuilding(Long playerId, MatchState matchState, Event event, MatchAllData matchAllData) {
        SetupFile setupFile = matchAllData.getSetupFile();
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+"! Change it to live for scoring");
        Inning inning;
        List<BatterCard> bcPlayers;
        inning = Util.getCurrentInning(matchState);
        bcPlayers = inning.getBattingCard().getBatters();
        if(inning.getStrikerId() != null && inning.getNonStrikerId() != null)
            throw new RuntimeConflictException("Both the Players are not out");
        if(playerId.equals(inning.getStrikerId()) || playerId.equals(inning.getNonStrikerId())) {
            throw new RuntimeConflictException("Player already batting");
        }
        if(setupFile.getSquads().getHomeTeamImpactPlayerDTO() != null ){
            if(playerId.equals(setupFile.getSquads().getHomeTeamImpactPlayerDTO().getImpactInPlayerId())){
                PlayerDTO impactIn = setupFile.getSquads().getHomeTeamActivePlayers().getPlayers()
                        .stream()
                        .filter(plyr -> plyr.getId().equals(playerId))
                        .findAny()
                        .orElseThrow(()->new ResourceNotFoundException("Player not found in active players"));
                BatterCard impactBatter = createImpactBatterCard(impactIn);
                boolean found = false;
                for(BatterCard bc: inning.getBattingCard().getBatters()){
                    if(bc.getBatter().getPlayerId().equals(playerId)){
                        bc.setIsImpactIn(true);
                        found = true;
                        break;
                    }
                }
                if(!found){
                    inning.getBattingCard().getBatters().add(impactBatter);
                }
            }
        }
        if(setupFile.getSquads().getAwayTeamImpactPlayerDTO() != null){
            if(playerId.equals(setupFile.getSquads().getAwayTeamImpactPlayerDTO().getImpactInPlayerId())){
                PlayerDTO impactIn = setupFile.getSquads().getAwayTeamActivePlayers().getPlayers()
                        .stream()
                        .filter(plyr -> plyr.getId().equals(playerId))
                        .findAny()
                        .orElseThrow(()->new ResourceNotFoundException("Player not found in active players"));
                BatterCard impactBatter = createImpactBatterCard(impactIn);
                boolean found = false;
                for(BatterCard bc: inning.getBattingCard().getBatters()){
                    if(bc.getBatter().getPlayerId().equals(playerId)){
                        bc.setIsImpactIn(true);
                        found = true;
                        break;
                    }
                }
                if(!found){
                    inning.getBattingCard().getBatters().add(impactBatter);
                }
            }
        }
        BatterCard batterCard = bcPlayers.stream().filter(plyr -> plyr.getBatter().getPlayerId().equals(playerId)).findAny().orElseThrow(()->new ResourceNotFoundException("Player not found in Battercard with id "+playerId));
        if(BattingStatus.OUT.equals(batterCard.getDismissal().getStatus())){
            throw new RuntimeConflictException("Batter already dismissed");
        }
        //creating partnership
        scoreCardService.createPartnershipCard(playerId,matchState, inning,event);
        int nextBatPosition = inning.getNextBattingPosition() == null ? 1 : inning.getNextBattingPosition();
        if (inning.getStrikerId() == null) {
            inning.setStrikerId(playerId);
            batterCard.setOnStrike(true);
        } else if (inning.getNonStrikerId() == null) {
            inning.setNonStrikerId(playerId);
            batterCard.setOnStrike(false);
        }
        //UPDATING CONTEXT METRICS OF PLAYER
        String entry = String.valueOf(inning.getScoreSummary().getRuns()) +"-"
                + String.valueOf(inning.getScoreSummary().getWickets());
        scoreCardService.updateContextMetricsOfBatter(batterCard, event, entry, null);
        batterCard.getDismissal().setStatus(BattingStatus.NOT_OUT);
        if(batterCard.getBatter().getBattingPosition() == null){
            batterCard.getBatter().setBattingPosition(nextBatPosition);
        }
        inning.setNextBattingPosition(nextBatPosition+1);
        Util.sortBattingCardByPosition(inning.getBattingCard());
        return batterCard;
    }

    public BowlingCard selectNewBowler(Long playerId, Event event) {
        MatchAllData matchAllData = jsonFileService.loadMatch(event.getMatchId());
        MatchState matchState = matchAllData.getMatchState();
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+"! Change it to live for scoring");
        SetupFile setupFile = matchAllData.getSetupFile();
        EventFile eventFile = matchAllData.getEventFile();
        Inning inning = Util.getCurrentInning(matchState);

        ActivePlayers bowlingTeamPlaying11 = matchState.getBattingTeamId().equals(setupFile.getTeams().getHomeTeam().getId())
                        ? setupFile.getSquads().getAwayTeamActivePlayers()
                        : setupFile.getSquads().getHomeTeamActivePlayers();


        // Validate bowler exists in bowling team
        PlayerDTO bowler = bowlingTeamPlaying11.getPlayers()
                .stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Player not found in playing 11 with id " + playerId
                        ));
        if(playerId.equals(inning.getCurrentBowlerId())){
            throw new RuntimeConflictException(
                    "Bowler cannot bowl consecutive overs"
            );
        }


        List<BowlerCard> bowlers = inning.getBowlingCard().getBowlers();

        Optional<BowlerCard> existingBowler = bowlers.stream()
                        .filter(b -> b.getBowler().getPlayerId().equals(playerId))
                        .findAny();

        existingBowler.ifPresent(blr -> {
            blr.setIsCurrentBowler(true);
        });


        if(existingBowler.isEmpty()){
            PlayerInfo playerInfo = PlayerInfo.builder()
                    .playerId(playerId)
                    .playerName(bowler.getFullName())
                    .build();
            BowlerCard newBowler = BowlerCard.builder()
                    .bowler(playerInfo)
                    .overs(null)
                    .wickets(0)
                    .economy(0.0)
                    .maidens(0)
                    .runsConceded(0)
                    .currentOverRuns(0)
                    .eachOver(new ArrayList<>())
                    .totalLegalDeliveriesBowled(0)
                    .isCurrentBowler(true)
                    .build();

            bowlers.add(newBowler);
        }
        for(BowlerCard bowlerCard : bowlers){
            if(bowlerCard.getBowler().getPlayerId().equals(inning.getCurrentBowlerId())){
                bowlerCard.setIsCurrentBowler(false);
            }
        }
        inning.setCurrentBowlerId(playerId);
        scoreCardService.createOverProgression(matchState,inning,event);
        jsonFileService.createMatchStateFile(matchState);
        jsonFileService.appendEvent(matchState, inning, event, eventFile);
        jsonFileService.createEventFile(matchState, eventFile);
        return inning.getBowlingCard();
    }
    public BowlingCard applyNewBowlerForMatchBuilding(Long playerId, MatchState matchState, Event event, MatchAllData matchAllData) {
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+"! Change it to live for scoring");
        SetupFile setupFile = matchAllData.getSetupFile();
        EventFile eventFile = matchAllData.getEventFile();
        Inning inning = Util.getCurrentInning(matchState);
        Long bowlingTeamId;
        if(event.getInningNumber() == 1){
            bowlingTeamId = setupFile.getToss().getBowlingTeamId();
        }else{
            bowlingTeamId = setupFile.getToss().getBattingTeamId();
        }

        ActivePlayers bowlingTeamPlaying11 = bowlingTeamId.equals(setupFile.getTeams().getHomeTeam().getId())
                ? setupFile.getSquads().getHomeTeamActivePlayers()
                : setupFile.getSquads().getAwayTeamActivePlayers();


        // Validate bowler exists in bowling team
        PlayerDTO bowler = bowlingTeamPlaying11.getPlayers()
                .stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Player not found in playing 11 with id " + playerId
                        ));
        if(playerId.equals(inning.getCurrentBowlerId())){
            throw new RuntimeConflictException(
                    "Bowler cannot bowl consecutive overs"
            );
        }


        List<BowlerCard> bowlers = inning.getBowlingCard().getBowlers();

        Optional<BowlerCard> existingBowler = bowlers.stream()
                .filter(b -> b.getBowler().getPlayerId().equals(playerId))
                .findAny();

        existingBowler.ifPresent(blr -> {
            blr.setIsCurrentBowler(true);
        });


        if(existingBowler.isEmpty()){
            PlayerInfo playerInfo = PlayerInfo.builder()
                    .playerId(playerId)
                    .playerName(bowler.getFullName())
                    .build();
            BowlerCard newBowler = BowlerCard.builder()
                    .bowler(playerInfo)
                    .overs(null)
                    .wickets(0)
                    .economy(0.0)
                    .maidens(0)
                    .runsConceded(0)
                    .currentOverRuns(0)
                    .eachOver(new ArrayList<>())
                    .totalLegalDeliveriesBowled(0)
                    .isCurrentBowler(true)
                    .build();

            bowlers.add(newBowler);
        }
        for(BowlerCard bowlerCard : bowlers){
            if(bowlerCard.getBowler().getPlayerId().equals(inning.getCurrentBowlerId())){
                bowlerCard.setIsCurrentBowler(false);
            }
        }
        inning.setCurrentBowlerId(playerId);
        scoreCardService.createOverProgression(matchState,inning,event);
        return inning.getBowlingCard();
    }
    public void updateTeamScore(Event event, MatchState matchState, Inning inning){
        //UPDATING IN SCORE CARD
        //UPDATE RUNS OF STATE FILE
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+"! Change it to live for scoring");

        if(inning.getStrikerId() == null || inning.getNonStrikerId() == null || inning.getCurrentBowlerId() == null){
            throw new RuntimeConflictException("Select Batter and Bowler");
        }
        int currentRuns = inning.getScoreSummary().getRuns() == null ? 0 : inning.getScoreSummary().getRuns();
        int batRuns = event.getRunOffBat() == null ? 0 : event.getRunOffBat();
        int extras = event.getExtrasRuns() == null ? 0 : event.getExtrasRuns();
        int subExtras = event.getSubExtrasRuns() == null ? 0 : event.getSubExtrasRuns();
        inning.getScoreSummary().setRuns(currentRuns + batRuns + extras + subExtras);

        //UPDATE EXTRAS
        ExtraType extraType = event.getExtraType()==null ? ExtraType.NONE : event.getExtraType();
        int wideRuns = inning.getExtras().getWides() == null ? 0 : inning.getExtras().getWides();
        int noBallRuns = inning.getExtras().getNoBalls() == null ? 0 : inning.getExtras().getNoBalls();
        int byesRuns = inning.getExtras().getByes() == null ? 0 : inning.getExtras().getByes();
        int legByesRuns = inning.getExtras().getLegByes() == null ? 0 : inning.getExtras().getLegByes();
        int penaltiesRuns = inning.getExtras().getPenalty() == null ? 0 : inning.getExtras().getPenalty();
        int subExtra = event.getSubExtrasRuns() == null ? 0 : event.getSubExtrasRuns();
        switch (event.getEventType()){
            case WIDE:
                inning.getExtras().setWides(wideRuns + event.getExtrasRuns());
                break;
            case NO_BALL:
                int extra = event.getExtrasRuns() == null ? 0 : event.getExtrasRuns();
                inning.getExtras().setNoBalls(noBallRuns + extra);
                break;
            case BYE:
                inning.getExtras().setByes(byesRuns + subExtra);
                break;
            case LEG_BYE:
                inning.getExtras().setLegByes(legByesRuns + subExtra);
                break;
            case PENALTY:
                inning.getExtras().setPenalty(penaltiesRuns + subExtra);
                break;
            case ANY_BALL:
                if(event.getExtraType() == ExtraType.WIDE){
                    inning.getExtras().setWides(wideRuns + event.getExtrasRuns());
                    if(event.getSubExtraType() != null && event.getSubExtraType() == ExtraType.BYE){
                        inning.getExtras().setByes(byesRuns + subExtra);
                    }else{
                        inning.getExtras().setLegByes(legByesRuns + subExtra);
                    }
                }else if(event.getExtraType() == ExtraType.NO_BALL){
                    int extraRun = event.getExtrasRuns() == null ? 0 : event.getExtrasRuns();
                    inning.getExtras().setNoBalls(noBallRuns + extraRun);
                    if(event.getSubExtraType() != null && event.getSubExtraType() == ExtraType.BYE){
                        inning.getExtras().setByes(byesRuns + subExtra);
                    }else{
                        inning.getExtras().setLegByes(legByesRuns + subExtra);
                    }
                }else if(event.getExtraType() == ExtraType.BYE){
                    inning.getExtras().setByes(byesRuns + subExtra);
                }else if(event.getExtraType() == ExtraType.LEG_BYE){
                    inning.getExtras().setLegByes(legByesRuns + subExtra);
                }
                break;
        }
        inning.getExtras().setTotal(
                (inning.getExtras().getWides() == null ? 0 : inning.getExtras().getWides()) +
                        (inning.getExtras().getNoBalls() == null ? 0 : inning.getExtras().getNoBalls()) +
                        (inning.getExtras().getByes() == null ? 0 : inning.getExtras().getByes()) +
                        (inning.getExtras().getLegByes() == null ? 0 : inning.getExtras().getLegByes()) +
                        (inning.getExtras().getPenalty() == null ? 0 : inning.getExtras().getPenalty())
        );
        inning.getScoreSummary().setRunRate(Util.calculateRunRate(inning.getScoreSummary().getRuns(), (inning.getScoreSummary().getOvers()*6)+inning.getScoreSummary().getBalls()));
    }

    @Override
    public boolean switchStrike(MatchState matchState){
        Inning inning = Util.getCurrentInning(matchState);
        Long strikerId = inning.getStrikerId();
        Long nonStrikerId = inning.getNonStrikerId();

        List<BatterCard> batters = inning.getBattingCard().getBatters();
        for(BatterCard batter : batters){
            if(batter.getBatter().getPlayerId().equals(inning.getStrikerId()) || batter.getBatter().getPlayerId().equals(inning.getNonStrikerId())){
                batter.setOnStrike(!batter.getOnStrike());
            }
        }

        inning.setStrikerId(nonStrikerId);
        inning.setNonStrikerId(strikerId);
        return true;
    }
    @Override
    public boolean swapStrikerDirectly(MatchState matchState){
        boolean isSwapped = switchStrike(matchState);
        jsonFileService.updateStateFile(matchState);
        return isSwapped;
    }
    @Override
    public void endOver(Event event) {
        MatchAllData matchAllData = jsonFileService.loadMatch(event.getMatchId());
        MatchState matchState = matchAllData.getMatchState();
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+"! Change it to live for scoring");

        EventFile eventFile = matchAllData.getEventFile();

        if(eventFile.getEvents().getLast().getBowlerId() == null) throw  new RuntimeConflictException("Bowler is not selected");

        Inning inning = Util.getCurrentInning(matchState);
        jsonFileService.appendEvent(matchState, inning, event, eventFile);
        jsonFileService.createMatchStateFile(matchState);
    }

    @Override
    public void impactPlayer(ImpactPlayerDTO impactPlayerDTO, Long matchId) {
        MatchAllData matchAllData = jsonFileService.loadMatch(matchId);
        SetupFile setupFile = matchAllData.getSetupFile();
        MatchState matchState = matchAllData.getMatchState();
        EventFile eventFile = matchAllData.getEventFile();
        Inning inning = Util.getCurrentInning(matchState);
        Event event = Event.builder()
                .eventType(IMPACT)
                .isImpact(true)
                .impactInPlayerId(impactPlayerDTO.getImpactInPlayerId())
                .impactOutPlayerId(impactPlayerDTO.getImpactOutPlayerId())
                .teamId(impactPlayerDTO.getTeamId())
                .build();
        jsonFileService.appendEvent(matchState, inning, event, eventFile);

//        if(setupFile.getSquads().getHomeTeamImpactPlayerDTO() != null && setupFile.getSquads().getAwayTeamImpactPlayerDTO() != null)
//            throw new RuntimeConflictException("Both teams have taken their impact");
//        if(impactPlayerDTO.getTeamId().equals(setupFile.getTeams().getHomeTeam().getId()) && setupFile.getSquads().getHomeTeamImpactPlayerDTO() != null)
//            throw new RuntimeConflictException("teams with id "+impactPlayerDTO.getTeamId()+" have taken their impact");
//        if(impactPlayerDTO.getTeamId().equals(setupFile.getTeams().getAwayTeam().getId())  && setupFile.getSquads().getAwayTeamImpactPlayerDTO() != null)
//            throw new RuntimeConflictException("teams with id "+impactPlayerDTO.getTeamId()+" have taken their impact");

        Substitutes substitutes;
        Playing11 playing11;
        ActivePlayers activePlayers;
        boolean isHomeTeam = false;
        if(impactPlayerDTO.getTeamId().equals(setupFile.getTeams().getHomeTeam().getId())){
            setupFile.getSquads().setHomeTeamImpactPlayerDTO(impactPlayerDTO);
            substitutes = setupFile.getSquads().getHomeTeamSubstitutes();
            activePlayers = setupFile.getSquads().getHomeTeamActivePlayers();
            isHomeTeam = true;
        }else{
            setupFile.getSquads().setAwayTeamImpactPlayerDTO(impactPlayerDTO);
            substitutes = setupFile.getSquads().getAwayTeamSubstitutes();
            activePlayers = setupFile.getSquads().getAwayTeamActivePlayers();
        }
        PlayerDTO playerIn = substitutes.getPlayers()
                .stream()
                .filter(plyr -> plyr.getId().equals(impactPlayerDTO.getImpactInPlayerId()))
                .findAny()
                .orElseThrow(()->new ResourceNotFoundException("Player not found in substitues"));
        activePlayers.getPlayers().add(playerIn);

        if(matchState.getBattingTeamId().equals(impactPlayerDTO.getTeamId())){
            Inning currentInning = Util.getCurrentInning(matchState);

            BatterCard impactBatter = createImpactBatterCard(playerIn);

            BatterCard impactOutBatter = currentInning.getBattingCard().getBatters().stream()
                                        .filter(bc -> bc.getBatter().getPlayerId().equals(impactPlayerDTO.getImpactOutPlayerId()))
                                        .findAny()
                                        .orElseThrow(()-> new ResourceNotFoundException("Player not found in battingcard with player "+impactPlayerDTO.getImpactOutPlayerId()));
            impactOutBatter.setIsImpactOut(true);

            currentInning.getBattingCard().getBatters().add(impactBatter);

            jsonFileService.updateStateFile(matchState);
        }
        if(isHomeTeam){
            setupFile.getSquads().setHomeTeamActivePlayers(activePlayers);
        }else{
            setupFile.getSquads().setAwayTeamActivePlayers(activePlayers);
        }
        matchAllData.setSetupFile(setupFile);
        jsonFileService.createEventFile(matchState,eventFile);
        jsonFileService.updateSetUpFile(setupFile);
    }
    public void impactPlayerForMatchRebuilding(SetupFile setupFile, MatchState matchState, EventFile eventFile, Event event, Long matchId){

        Substitutes substitutes;
        Playing11 playing11;
        ActivePlayers activePlayers;
        boolean isHomeTeam = false;
        boolean found = false;
        if(event.getTeamId().equals(setupFile.getTeams().getHomeTeam().getId())){
            if(setupFile.getSquads().getHomeTeamImpactPlayerDTO() == null){
                ImpactPlayerDTO impactPlayerDTO = ImpactPlayerDTO.builder()
                        .impactInPlayerId(event.getImpactInPlayerId())
                        .impactOutPlayerId(event.getImpactOutPlayerId())
                        .teamId(event.getTeamId())
                        .build();
                setupFile.getSquads().setHomeTeamImpactPlayerDTO(impactPlayerDTO);
            }

            substitutes = setupFile.getSquads().getHomeTeamSubstitutes();
            activePlayers = setupFile.getSquads().getHomeTeamActivePlayers();
            isHomeTeam = true;
        }else{
            if(setupFile.getSquads().getAwayTeamImpactPlayerDTO() == null){
                ImpactPlayerDTO impactPlayerDTO = ImpactPlayerDTO.builder()
                        .impactInPlayerId(event.getImpactInPlayerId())
                        .impactOutPlayerId(event.getImpactOutPlayerId())
                        .teamId(event.getTeamId())
                        .build();
                setupFile.getSquads().setAwayTeamImpactPlayerDTO(impactPlayerDTO);
            }

            substitutes = setupFile.getSquads().getAwayTeamSubstitutes();
            activePlayers = setupFile.getSquads().getAwayTeamActivePlayers();
        }
        PlayerDTO playerIn = substitutes.getPlayers()
                .stream()
                .filter(plyr -> plyr.getId().equals(event.getImpactInPlayerId()))
                .findAny()
                .orElseThrow(()->new ResourceNotFoundException("Player not found in substitues"));
        for(PlayerDTO plyr: activePlayers.getPlayers()){
            if(plyr.getId().equals(event.getImpactInPlayerId())){
                found = true;
                break;
            }
        }
        if(!found){
            activePlayers.getPlayers().add(playerIn);
        }
        for(BatterCard bc : matchState.getScoreCard().getInnings().getFirst().getBattingCard().getBatters()){
            if(bc.getBatter().getPlayerId().equals(event.getImpactOutPlayerId())){
                bc.setIsImpactOut(true);
                bc.setIsImpactOut(false);
                break;
            }
        }
        if(matchState.getScoreCard().getInnings().size()>1){
            for(BatterCard bc : matchState.getScoreCard().getInnings().get(1).getBattingCard().getBatters()){
                if(bc.getBatter().getPlayerId().equals(event.getImpactOutPlayerId())){
                    bc.setIsImpactOut(true);
                    bc.setIsImpactIn(false);
                    break;
                }
            }
        }
        if(isHomeTeam){
            setupFile.getSquads().setHomeTeamActivePlayers(activePlayers);
        }else{
            setupFile.getSquads().setAwayTeamActivePlayers(activePlayers);
        }
    }
    private BatterCard createImpactBatterCard(PlayerDTO plyr){

        PlayerInfo playerInfo = PlayerInfo.builder()
                .playerId(plyr.getId())
                .playerName(plyr.getFullName())
                .battingPosition(null)
                .build();

        DismissalInfo dismissalInfo = DismissalInfo.builder()
                .status(BattingStatus.STILL_TO_BAT)
                .build();

        return BatterCard.builder()
                .batter(playerInfo)
                .scoring(null)
                .dismissal(dismissalInfo)
                .phases(null)
                .control(null)
                .context(null)
                .onStrike(false)
                .isImpactIn(true)
                .isImpactOut(false)
                .build();
    }
}
