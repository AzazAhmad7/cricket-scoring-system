package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.PlayerDTO;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.entities.enums.BattingStatus;
import com.cricket.scoring.entities.enums.EventType;
import com.cricket.scoring.entities.enums.ExtraType;
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
        EventFile eventFile = jsonFileService.getEventsFromMemory(event.getMatchId());

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
        System.out.println(newEvent.getDismissedType());
        if(newEvent.getIsWicket() != null && newEvent.getIsWicket()){
            inning.getScoreSummary().setWickets(wickets+1);
            if(Util.isBowlerCreditWicket(newEvent.getDismissedType())){
                scoreCardService.updateFOW(matchState.getStrikerId(), matchState, inning, newEvent);
                matchState.setStrikerId(null);
            }
        }
        jsonFileService.updateStateFile(matchState);
        if(newEvent.getTotalLegalBallsBowled()>0 && newEvent.getTotalLegalBallsBowled() % 6 == 0){
            Event event1 = Event.builder()
                    .matchId(newEvent.getMatchId())
                    .eventType(END_OVER)
                    .build();
            endOver(event1);
        }
        jsonFileService.createEventFile(matchState, jsonFileService.getEventsFromMemory(event.getMatchId()));
    }

    @Override
    public BatterCard selectNewBatter(Long playerId, Event event) {
        System.out.println("PlayerScoring "+playerId+" "+event);
        MatchState matchState = jsonFileService.getMatchStateFromMemory(event.getMatchId());
        EventFile eventFile = jsonFileService.getEventsFromMemory(event.getMatchId());

        if(matchState.getStrikerId() != null && matchState.getNonStrikerId() != null)
            throw new RuntimeConflictException("Both the Players are not out");
        if(playerId.equals(matchState.getStrikerId()) || playerId.equals(matchState.getNonStrikerId())) {
            throw new RuntimeConflictException("Player already batting");
        }
        Inning inning = Util.getCurrentInning(matchState);
        List<BatterCard> bcPlayers = inning.getBattingCard().getBatters();
        BatterCard batterCard = bcPlayers.stream().filter(plyr -> plyr.getBatter().getPlayerId().equals(playerId)).findAny().orElseThrow(()->new ResourceNotFoundException("Player not found in Battercard"));
        if(batterCard.getDismissal().getStatus() == BattingStatus.OUT){
            throw new RuntimeConflictException("Batter already dismissed");
        }
        //creating partnership
        scoreCardService.createPartnershipCard(playerId,matchState, inning,event);
        int nextBatPosition = matchState.getNextBattingPosition() == null ? 1 : matchState.getNextBattingPosition();
        if (matchState.getStrikerId() == null) {
            matchState.setStrikerId(playerId);
            batterCard.setOnStrike(true);
        } else if (matchState.getNonStrikerId() == null) {
            matchState.setNonStrikerId(playerId);
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
        matchState.setNextBattingPosition(nextBatPosition+1);
        jsonFileService.createMatchStateFile(matchState);
        jsonFileService.appendEvent(matchState, inning, event, eventFile);
        jsonFileService.createEventFile(matchState, eventFile);
        return batterCard;
    }

    public BowlingCard selectNewBowler(Long playerId, Event event) {
        System.out.println("Batter Id "+playerId+" event "+event);
        MatchState matchState = jsonFileService.getMatchStateFromMemory(event.getMatchId());
        SetupFile setupFile = jsonFileService.getSetupFileFromMemory(event.getMatchId());
        EventFile eventFile = jsonFileService.getEventsFromMemory(event.getMatchId());

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
        if(playerId.equals(matchState.getCurrentBowlerId())){
            throw new RuntimeConflictException(
                    "Bowler cannot bowl consecutive overs"
            );
        }
        Inning inning = Util.getCurrentInning(matchState);

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
            if(bowlerCard.getBowler().getPlayerId().equals(matchState.getCurrentBowlerId())){
                bowlerCard.setIsCurrentBowler(false);
            }
        }
        matchState.setCurrentBowlerId(playerId);
        scoreCardService.createOverProgression(matchState,inning,event);
        jsonFileService.createMatchStateFile(matchState);
        jsonFileService.appendEvent(matchState, inning, event, eventFile);
        jsonFileService.createEventFile(matchState, eventFile);
        return inning.getBowlingCard();
    }

    public void updateTeamScore(Event event, MatchState matchState, Inning inning){
        //UPDATING IN SCORE CARD
        //UPDATE RUNS OF STATE FILE

        if(matchState.getStrikerId() == null || matchState.getNonStrikerId() == null || matchState.getCurrentBowlerId() == null){
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
            default:
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
        inning.getExtras().setTotal(wideRuns+noBallRuns+byesRuns+legByesRuns+penaltiesRuns);
        inning.getScoreSummary().setRunRate(Util.calculateRunRate(inning.getScoreSummary().getRuns(), (inning.getScoreSummary().getOvers()*6)+inning.getScoreSummary().getBalls()));
    }

    public void switchStrike(MatchState matchState, Inning inning){
        Long strikerId = matchState.getStrikerId();
        Long nonStrikerId = matchState.getNonStrikerId();

        List<BatterCard> batters = inning.getBattingCard().getBatters();
        for(BatterCard batter : batters){
            if(batter.getBatter().getPlayerId().equals(matchState.getStrikerId()) || batter.getBatter().getPlayerId().equals(matchState.getNonStrikerId())){
                batter.setOnStrike(!batter.getOnStrike());
            }
        }

        matchState.setStrikerId(nonStrikerId);
        matchState.setNonStrikerId(strikerId);
    }
    @Override
    public void endOver(Event event) {
        MatchState matchState = jsonFileService.getMatchStateFromMemory(event.getMatchId());
        EventFile eventFile = jsonFileService.getEventsFromMemory(event.getMatchId());

        if(eventFile.getEvents().getLast().getBowlerId() == null) throw  new RuntimeConflictException("Bowler is not selected");

        Inning inning = Util.getCurrentInning(matchState);
        jsonFileService.appendEvent(matchState, inning, event, eventFile);
        jsonFileService.createMatchStateFile(matchState);
    }
}
