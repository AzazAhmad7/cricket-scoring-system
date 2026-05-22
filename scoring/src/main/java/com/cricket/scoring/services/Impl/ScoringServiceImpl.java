package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.ImpactPlayerDTO;
import com.cricket.scoring.dtos.PlayerDTO;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.dtos.TeamDTO;
import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.enums.*;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.exceptions.RuntimeConflictException;
import com.cricket.scoring.services.JsonFileService;
import com.cricket.scoring.services.MatchService;
import com.cricket.scoring.services.ScoreCardService;
import com.cricket.scoring.services.ScoringService;
import com.cricket.scoring.utils.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.cricket.scoring.entities.enums.EventType.*;
import static com.cricket.scoring.entities.enums.ExtraType.PENALTY;


@Service
@RequiredArgsConstructor
public class ScoringServiceImpl implements ScoringService {

    private final JsonFileService jsonFileService;
    private final ScoreCardService scoreCardService;

    @Override
    public void scoreBall(Event event) {
        MatchState matchState = jsonFileService.getMatchStateFromMemory(event.getMatchId());
        SetupFile setupFile = jsonFileService.getSetupFileFromMemory(event.getMatchId());
        EventFile eventFile = jsonFileService.getEventsFromMemory(event.getMatchId());
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+"! Change it to live for scoring");
        if((eventFile.getEvents().getLast().getTotalLegalBallsBowled() >= Util.overBallsToBalls(setupFile.getRules().getOvers(), 0))
        || matchState.getScoreCard().getInnings().get(matchState.getCurrentInningNumber()-1).getScoreSummary().getWickets() >= 10)
            throw new RuntimeConflictException("Inning ended");
        if(matchState.getMatchStatus() == MatchStatus.COMPLETED)
            throw new RuntimeConflictException("Matches ended");

        Inning inning = Util.getCurrentInning(matchState);
        jsonFileService.appendEvent(matchState, inning, event, eventFile);
        EventFile updatedEventFile = jsonFileService.getEventsFromMemory(event.getMatchId());
        Event newEvent = updatedEventFile.getEvents().getLast();
        updateTeamScore(newEvent, matchState, inning);
        scoreCardService.updateBattingCard(matchState, inning, newEvent);
        scoreCardService.updatePartnership(matchState,inning, newEvent);
        scoreCardService.updateBowlingCard(matchState, inning, newEvent);
        scoreCardService.updateOverProgression(matchState, inning, newEvent);
        scoreCardService.updatePhaseBreakDown(matchState, inning, newEvent);
        scoreCardService.updateInningControlMetrics(matchState, inning, newEvent);

        newEvent.setTotalLegalBallsBowled(Util.overBallsToBalls(inning.getScoreSummary().getOvers(), inning.getScoreSummary().getBalls()));
        //SWITCHING STRIKE OF BATTER
        boolean changeStrike = false;

        if(newEvent.getRunOffBat() != null && newEvent.getRunOffBat() % 2 != 0){
            changeStrike = true;
        }
        if(newEvent.getSubExtrasRuns() != null && (newEvent.getExtraType() == ExtraType.BYE || newEvent.getExtraType() == ExtraType.LEG_BYE) && newEvent.getSubExtrasRuns() % 2 != 0){
            changeStrike = true;
        }
        if(changeStrike){
            switchStrike(matchState, inning);
        }
        //UPDATE WICKET OF STATE FILE
        int wickets = inning.getScoreSummary().getWickets() == null ? 0 : inning.getScoreSummary().getWickets();
        if(newEvent.getIsWicket() != null && newEvent.getIsWicket()){
            inning.getScoreSummary().setWickets(wickets+1);
            scoreCardService.updateFOW(event.getDismissedPlayerId(), matchState, inning, newEvent);
            if(event.getDismissedPlayerId().equals(inning.getStrikerId())){
                inning.setStrikerId(null);
            }else if(event.getDismissedPlayerId().equals(inning.getNonStrikerId())){
                inning.setNonStrikerId(null);
            }
        }
        jsonFileService.updateStateFile(matchState);
        BowlerCard currentBowler = matchState.getScoreCard().getInnings().get(matchState.getCurrentInningNumber()-1).getBowlingCard().getBowlers().stream()
                .filter(bc -> bc.getIsCurrentBowler().equals(true))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("Bowler not found"));
        if(currentBowler.getTotalLegalDeliveriesBowled()>0 && newEvent.getTotalLegalBallsBowled()>0 && newEvent.getTotalLegalBallsBowled() % 6 == 0){
            Event event1 = Event.builder()
                    .matchId(newEvent.getMatchId())
                    .eventType(END_OVER)
                    .build();
            endOver(event1);
        }
        if (inning.getInningNumber() == 1) {

            int ballsBowled = newEvent.getTotalLegalBallsBowled();
            int totalBallsToBeBowled = Util.overBallsToBalls(setupFile.getRules().getOvers(), 0);

            // First innings finished: create target for second innings
            if (ballsBowled >= totalBallsToBeBowled || inning.getScoreSummary().getWickets() >= 10) {

                int targetRuns = inning.getScoreSummary().getRuns() + 1;
                int totalBalls = Util.overBallsToBalls( setupFile.getRules().getOvers(), 0);

                TargetDTO targetDTO = TargetDTO.builder()
                        .targetRuns(targetRuns)
                        .runsRemaining(targetRuns)
                        .totalBalls(totalBalls)
                        .ballsRemaining(totalBalls)
                        .requiredRunRate(Util.requiredRunRate(targetRuns, totalBalls) )
                        .wicketsRemaining(10)
                        .targetAchieved(false)
                        .build();

                matchState.setTargetDTO(targetDTO);
                jsonFileService.updateStateFile(matchState);
            }

        } else {

            int ballsBowled = newEvent.getTotalLegalBallsBowled();
            int totalBallsToBeBowled = Util.overBallsToBalls(setupFile.getRules().getOvers(), 0);

            int runs = inning.getScoreSummary().getRuns();
            int thisInningWickeets = inning.getScoreSummary().getWickets();

            int target = matchState.getTargetDTO().getTargetRuns();
            int runsRemaining = target - runs;
            int wicketsRemaining = 10 - thisInningWickeets;

            boolean targetAchieved = runs >= target;
            boolean allOut = thisInningWickeets >= 10;
            boolean oversCompleted = ballsBowled >= totalBallsToBeBowled;

            MatchResultDTO matchResultDTO = null;

            // Determine batting team (current chasing team)
            Team battingTeam = matchState.getBattingTeamId().equals(setupFile.getTeams().getHomeTeam().getId())
                            ? setupFile.getTeams().getHomeTeam()
                            : setupFile.getTeams().getAwayTeam();

            // Determine bowling team (defending team)
            Team bowlingTeam = matchState.getBattingTeamId().equals(setupFile.getTeams().getHomeTeam().getId())
                            ? setupFile.getTeams().getAwayTeam()
                            : setupFile.getTeams().getHomeTeam();

            // --------------------------------------------------
            // 1. Chasing team wins
            // --------------------------------------------------
            if (targetAchieved) {

                matchResultDTO = MatchResultDTO.builder()
                        .resultType(MatchResultType.WIN)
                        .winningTeamId(battingTeam.getId())
                        .winningTeamName(battingTeam.getName())
                        .marginType(ResultMarginType.WICKETS)
                        .marginValue(wicketsRemaining)
                        .inningsCount(null)
                        .summary(
                                battingTeam.getName()
                                        + " won by "
                                        + wicketsRemaining
                                        + " wickets"
                        )
                        .build();
            }

            // --------------------------------------------------
            // 2. Defending team wins
            // --------------------------------------------------
            else if ((allOut || oversCompleted) && runs < target - 1) {

                matchResultDTO = MatchResultDTO.builder()
                        .resultType(MatchResultType.WIN)
                        .winningTeamId(bowlingTeam.getId())
                        .winningTeamName(bowlingTeam.getName())
                        .marginType(ResultMarginType.RUNS)
                        .marginValue(runsRemaining)
                        .inningsCount(null)
                        .summary(
                                bowlingTeam.getName()
                                        + " won by "
                                        + runsRemaining
                                        + " runs"
                        )
                        .build();
            }

            // --------------------------------------------------
            // 3. Match tied
            // --------------------------------------------------
            else if ((allOut || oversCompleted) && runs == target - 1) {

                matchResultDTO = MatchResultDTO.builder()
                        .resultType(MatchResultType.TIE)
                        .winningTeamId(null)
                        .winningTeamName(null)
                        .marginType(null)
                        .marginValue(null)
                        .inningsCount(null)
                        .summary("Match tied")
                        .build();
            }

            // Save result if match is finished
            if (matchResultDTO != null) {
                setupFile.getMatchInfo().setStatus(MatchStatus.COMPLETED);
                matchState.setMatchStatus(MatchStatus.COMPLETED);
                matchState.setMatchResultDTO(matchResultDTO);
                jsonFileService.updateStateFile(matchState);
            }
        }
        jsonFileService.createEventFile(matchState, jsonFileService.getEventsFromMemory(event.getMatchId()));
    }

    @Override
    public BatterCard selectNewBatter(Long playerId, Event event) {
        MatchState matchState = jsonFileService.getMatchStateFromMemory(event.getMatchId());
        EventFile eventFile = jsonFileService.getEventsFromMemory(event.getMatchId());
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
        if(batterCard.getDismissal().getStatus() == BattingStatus.OUT){
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
        if(batterCard.getDismissal() == null){
            batterCard.setDismissal(new DismissalInfo());
        }
        batterCard.getDismissal().setStatus(BattingStatus.NOT_OUT);
        if(batterCard.getBatter().getBattingPosition() == null){
            batterCard.getBatter().setBattingPosition(nextBatPosition);
        }
        inning.setNextBattingPosition(nextBatPosition+1);
        jsonFileService.createMatchStateFile(matchState);
        jsonFileService.appendEvent(matchState, inning, event, eventFile);
        jsonFileService.createEventFile(matchState, eventFile);
        return batterCard;
    }

    public BowlingCard selectNewBowler(Long playerId, Event event) {
        MatchState matchState = jsonFileService.getMatchStateFromMemory(event.getMatchId());
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+"! Change it to live for scoring");
        SetupFile setupFile = jsonFileService.getSetupFileFromMemory(event.getMatchId());
        EventFile eventFile = jsonFileService.getEventsFromMemory(event.getMatchId());
        Inning inning = Util.getCurrentInning(matchState);

        Playing11 bowlingTeamPlaying11 = setupFile.getTeams().getBattingTeamId().equals(setupFile.getTeams().getHomeTeam().getId())
                        ? setupFile.getSquads().getAwayTeamPlaying11()
                        : setupFile.getSquads().getHomeTeamPlaying11();


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
        int thisOverBalls = inning.getScoreSummary().getBalls() == null ? 0 : inning.getScoreSummary().getBalls();
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
        if(event.getExtraType() != ExtraType.WIDE && event.getExtraType() != ExtraType.NO_BALL && event.getExtraType() != ExtraType.PENALTY){
            int overs = inning.getScoreSummary().getOvers() == null ? 0 : inning.getScoreSummary().getOvers();
            inning.getScoreSummary().setBalls(thisOverBalls+1);
            event.setBallInOver(thisOverBalls+1);
            if(inning.getScoreSummary().getBalls() == 6){
                inning.getScoreSummary().setOvers(overs+1);
                inning.getScoreSummary().setBalls(0);

                event.setOversCompleted(overs+1);
                event.setBallInOver(0);
                switchStrike(matchState, inning);
            }
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

    public void switchStrike(MatchState matchState, Inning inning){
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
    }
    @Override
    public void endOver(Event event) {
        MatchState matchState = jsonFileService.getMatchStateFromMemory(event.getMatchId());
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+"! Change it to live for scoring");

        EventFile eventFile = jsonFileService.getEventsFromMemory(event.getMatchId());

        if(eventFile.getEvents().getLast().getBowlerId() == null) throw  new RuntimeConflictException("Bowler is not selected");

        Inning inning = Util.getCurrentInning(matchState);
        jsonFileService.appendEvent(matchState, inning, event, eventFile);
        jsonFileService.createMatchStateFile(matchState);
    }

    @Override
    public void impactPlayer(ImpactPlayerDTO impactPlayerDTO, Long matchId) {
        SetupFile setupFile = jsonFileService.loadSetupFile(matchId);
        Substitutes substitutes;
        Playing11 playing11;
        boolean isHomeTeam = false;
        if(impactPlayerDTO.getTeamId().equals(setupFile.getTeams().getHomeTeam().getId())){
            setupFile.getSquads().setHomeTeamImpactPlayerDTO(impactPlayerDTO);
            substitutes = setupFile.getSquads().getHomeTeamSubstitutes();
            playing11 = setupFile.getSquads().getHomeTeamPlaying11();
            isHomeTeam = true;
        }else{
            setupFile.getSquads().setHomeTeamImpactPlayerDTO(impactPlayerDTO);
            substitutes = setupFile.getSquads().getAwayTeamSubstitutes();
            playing11 = setupFile.getSquads().getAwayTeamPlaying11();
        }
        PlayerDTO playerIn = substitutes.getPlayers()
                .stream()
                .filter(plyr -> plyr.getId().equals(impactPlayerDTO.getImpactInPlayerId()))
                .findAny()
                .orElseThrow(()->new ResourceNotFoundException("Player not found in substitues"));
        PlayerDTO playerOut = playing11.getPlayers()
                .stream()
                .filter(plyr -> plyr.getId().equals(impactPlayerDTO.getImpactOutPlayerId()))
                .findAny()
                .orElseThrow(()->new ResourceNotFoundException("Player not found in playing11"));

        substitutes.getPlayers().remove(playerIn);
        playing11.getPlayers().add(playerIn);
        playing11.getPlayers().remove(playerOut);
        substitutes.getPlayers().add(playerOut);
        if(isHomeTeam){
            setupFile.getSquads().setHomeTeamPlaying11(playing11);
            setupFile.getSquads().setHomeTeamSubstitutes(substitutes);
        }else{
            setupFile.getSquads().setAwayTeamPlaying11(playing11);
            setupFile.getSquads().setAwayTeamSubstitutes(substitutes);
        }
        jsonFileService.updateSetUpFile(setupFile);
    }
}
