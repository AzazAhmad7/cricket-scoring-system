package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.PlayerDTO;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.entities.Match;
import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.enums.BattingStatus;
import com.cricket.scoring.entities.enums.DismissalType;
import com.cricket.scoring.entities.enums.EventType;
import com.cricket.scoring.entities.enums.ExtraType;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.exceptions.RuntimeConflictException;
import com.cricket.scoring.services.JsonFileService;
import com.cricket.scoring.services.PlayerService;
import com.cricket.scoring.services.ScoreCardService;
import com.cricket.scoring.services.ScoringService;
import com.cricket.scoring.utils.Util;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.ast.tree.expression.Over;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.ldap.Control;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.cricket.scoring.entities.enums.DismissalType.*;

@Service
@RequiredArgsConstructor
public class ScoreCardServiceImpl implements ScoreCardService {

    private final PlayerService playerService;

    @Override
    public BatterCard updateBattingCard(MatchState matchState, Inning inning, Event event) {
        List<BatterCard> batterCards = matchState.getScoreCard().getInnings().get(matchState.getCurrentInningNumber()-1).getBattingCard().getBatters();
        BatterCard batterCard = batterCards.stream()
                .filter(bc -> bc.getBatter().getPlayerId().equals(inning.getStrikerId()))
                .findAny()
                .orElseThrow(()->new ResourceNotFoundException("Player not found in batterCard with id "+inning.getStrikerId()));


        //UPDATING SCORING STATS
        updateScoringStatsOfBatter(batterCard, event);

        //UPDATE DISMISSAL OF BATTER
        if(event.getIsWicket() != null && event.getIsWicket()){
            BatterCard dismissedBatterCard = batterCards.stream()
                    .filter(bc -> bc.getBatter().getPlayerId().equals(event.getDismissedPlayerId()))
                    .findAny()
                    .orElseThrow(()->new ResourceNotFoundException("Player not found in batterCard with id "+inning.getStrikerId()));
            BowlerCard bowlerCard = inning.getBowlingCard().getBowlers().stream()
                    .filter(bowler -> bowler.getBowler().getPlayerId() == inning.getCurrentBowlerId())
                    .findAny()
                    .orElseThrow(()->new ResourceNotFoundException("Bowler not found"));
            updateDismissalOfBatter(matchState, bowlerCard, dismissedBatterCard, event);
        }

        //UPDATING PHASE BREAKDOWN OF PLAYER
        updatePhaseBreakdownOfBatter(matchState, batterCard, event);

        //UPDATING CONTROL METRICS OF PLAYER
        updateControlMetricsOfBatter(batterCard, event);

        return batterCard;
    }
    @Override
    public BowlingCard updateBowlingCard(MatchState matchState, Inning inning, Event event) {
        List<BowlerCard> bowlerCards = inning.getBowlingCard().getBowlers();
        BowlerCard bowlerCard = bowlerCards.stream()
                .filter(bowler -> bowler.getBowler().getPlayerId().equals(inning.getCurrentBowlerId()))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("Bowler not found with id "+inning.getCurrentBowlerId()));


        int ballsBowled = bowlerCard.getTotalLegalDeliveriesBowled() == null ? 0 : bowlerCard.getTotalLegalDeliveriesBowled();
        boolean isLegalDelivery = event.getExtraType() != ExtraType.WIDE && event.getExtraType() != ExtraType.NO_BALL && event.getExtraType() != ExtraType.PENALTY;
        if(isLegalDelivery){
            ballsBowled+=1;
            bowlerCard.setTotalLegalDeliveriesBowled(ballsBowled);
        }
        //EACH OVER IN BOWLER CARD
        int currentOverIndex;

        if(isLegalDelivery){
            currentOverIndex = (ballsBowled - 1) / 6;
        } else {
            // wides/no-balls stay in same over
            currentOverIndex = ballsBowled / 6;
        }
        List<OverEvent> overs = bowlerCard.getEachOver();

        // Create over if not exists
        if (overs.size() <= currentOverIndex) {
            OverEvent over = OverEvent.builder()
                    .overNumber(currentOverIndex+1)
                    .deliveries(new ArrayList<>())
                    .build();
            overs.add(over);
        }
        OverEvent currentOver = overs.get(currentOverIndex);

        BallEvent ballEvent = new BallEvent();
        String ballResult;
        if(event.getIsWicket() != null && event.getIsWicket() && event.getExtraType() == null){
            ballResult = "W";
            int batterRuns = 0;
            if(event.getRunOffBat() != null && event.getRunOffBat() > 0){
                ballResult+="+"+event.getRunOffBat();
                batterRuns+=event.getRunOffBat();
            }
            ballEvent.setDisplay(ballResult);
            ballEvent.setWicket(true);
            ballEvent.setRunsOffBat(batterRuns);
            ballEvent.setBowlerRuns(batterRuns);
        }
        else if(event.getEventType() != null && (event.getEventType() == EventType.WIDE)){
            ballResult="Wd";
            int runs = event.getExtrasRuns();
            boolean isWicket = false;
            if(event.getSubExtrasRuns() != null && event.getSubExtrasRuns() > 0){
                ballResult+="+"+event.getSubExtrasRuns();
            }
            if(event.getIsWicket() != null && event.getIsWicket()){
                ballResult+="+"+"W";
                isWicket = true;
            }
            ballEvent.setWicket(isWicket);
            ballEvent.setDisplay(ballResult);
            ballEvent.setRunsOffBat(0);
            ballEvent.setBowlerRuns(runs);
        }
        else if(event.getEventType() != null && event.getEventType().equals(EventType.NO_BALL)){
            int runs = event.getExtrasRuns();
            int runsOfBat = 0;
            boolean isWicket = false;
            ballResult="Nb";
            if(event.getRunOffBat() != null && event.getRunOffBat() > 0){
                ballResult += "+"+event.getRunOffBat();
                runs+=event.getRunOffBat();
                runsOfBat+=event.getRunOffBat();
            }
            if(event.getSubExtrasRuns() != null && event.getSubExtrasRuns() > 0){
                ballResult+="+"+event.getSubExtrasRuns();
            }
            if(event.getIsWicket() != null && event.getIsWicket()){
                ballResult+="+"+"W";
                isWicket = true;
            }
            ballEvent.setDisplay(ballResult);
            ballEvent.setWicket(isWicket);
            ballEvent.setRunsOffBat(runsOfBat);
            ballEvent.setBowlerRuns(runs);
        }
        else if(event.getEventType() != null && event.getEventType().equals(EventType.BYE)){
            ballResult= event.getSubExtrasRuns()+"B";
            boolean isWicket = false;
            if(event.getIsWicket() != null && event.getIsWicket()){
                ballResult+="+"+"W";
                isWicket = true;
            }
            ballEvent.setDisplay(ballResult);
            ballEvent.setWicket(isWicket);
            ballEvent.setRunsOffBat(0);
            ballEvent.setBowlerRuns(0);
        }
        else if(event.getEventType() != null && event.getEventType().equals(EventType.LEG_BYE)){
            ballResult=event.getSubExtrasRuns()+"Lb";
            boolean isWicket = false;
            if(event.getIsWicket() != null && event.getIsWicket()){
                ballResult+="+"+"W";
                isWicket = true;
            }
            ballEvent.setDisplay(ballResult);
            ballEvent.setWicket(isWicket);
            ballEvent.setRunsOffBat(0);
            ballEvent.setBowlerRuns(0);
        }else if(event.getEventType() != null && (event.getEventType() == EventType.ANY_BALL)){
            ballResult="";
            int runs = event.getExtrasRuns();
            int runsOfBat = 0;
            if(event.getExtraType() != null && event.getExtraType() == ExtraType.WIDE){
                ballResult="Wd";
            }else if(event.getExtraType() != null && event.getExtraType() == ExtraType.NO_BALL){
                ballResult="Nb";
                runs+=event.getRunOffBat();
                runsOfBat+=event.getRunOffBat();
            }else if(event.getExtraType() != null && event.getExtraType() == ExtraType.BYE){
                ballResult="B";
            }else if(event.getExtraType() != null && event.getExtraType() == ExtraType.LEG_BYE){
                ballResult="Lb";
            }


            boolean isWicket = false;
            if((event.getExtraType() != null && event.getExtraType() == ExtraType.BYE) || (event.getExtraType() != null && event.getExtraType() == ExtraType.LEG_BYE)){
                if(event.getSubExtrasRuns() != null && event.getSubExtrasRuns() > 0){
                    ballResult=event.getSubExtrasRuns()+runs+ballResult;
                }
            }else{
                if(event.getExtraType() != null && event.getExtraType() == ExtraType.NO_BALL){
                    if(runsOfBat > 0){
                        ballResult=runs+ballResult;
                    }
                    if(event.getSubExtrasRuns() != null && event.getSubExtrasRuns() > 0){
                        ballResult+="+"+event.getSubExtrasRuns();
                    }
                }else{
                    if(event.getSubExtrasRuns() != null && event.getSubExtrasRuns() > 0){
                        ballResult+="+"+event.getSubExtrasRuns();
                    }
                }
            }
            if(event.getIsWicket() != null && event.getIsWicket()){
                ballResult+="+"+"W";
                isWicket = true;
            }
            ballEvent.setWicket(isWicket);
            ballEvent.setDisplay(ballResult);
            ballEvent.setRunsOffBat(runsOfBat);
            ballEvent.setBowlerRuns(runs);
        }
        else{
            ballResult=String.valueOf(event.getRunOffBat());
            ballEvent.setDisplay(ballResult);
            ballEvent.setWicket(false);
            ballEvent.setRunsOffBat(event.getRunOffBat());
            ballEvent.setBowlerRuns(event.getRunOffBat());
        }

        currentOver.getDeliveries().add(ballEvent);

        int runsConceded = bowlerCard.getRunsConceded() == null ? 0 : bowlerCard.getRunsConceded();

        int overRuns = bowlerCard.getCurrentOverRuns() == null ? 0 : bowlerCard.getCurrentOverRuns();

        int ballRuns = ballEvent.getBowlerRuns();

        runsConceded += ballRuns;
        overRuns += ballRuns;

        int maidens = bowlerCard.getMaidens() == null ? 0 : bowlerCard.getMaidens();
        if(ballsBowled > 0 && ballsBowled % 6 == 0){
            if(overRuns == 0){
                maidens++;
            }
            overRuns = 0; // reset next over
        }
        bowlerCard.setCurrentOverRuns(overRuns);

        int wickets = bowlerCard.getWickets() == null ? 0 : bowlerCard.getWickets();
        if(event.getIsWicket() != null && event.getIsWicket() && Util.isBowlerCreditWicket(event.getDismissedType())){
            wickets+=1;
        }

        bowlerCard.setOvers(Util.overBalls(ballsBowled));
        bowlerCard.setRunsConceded(runsConceded);
        bowlerCard.setWickets(wickets);
        bowlerCard.setMaidens(maidens);
        bowlerCard.setEconomy(Util.calculateEconomy(runsConceded, ballsBowled));
        bowlerCard.setIsCurrentBowler(true);
        return inning.getBowlingCard();
    }

    public ContextMetrics updateContextMetricsOfBatter(BatterCard batterCard, Event event, String entry, String exit) {
        if(batterCard.getContext() == null){
            ContextMetrics contextMetrics = ContextMetrics.builder()
                    .entryScore(null)
                    .exitScore(null)
                    .build();
            batterCard.setContext(contextMetrics);
        }
        if(batterCard.getContext().getEntryScore() == null){
            batterCard.getContext().setEntryScore(entry);
        }
        if(batterCard.getContext().getExitScore() == null){
            batterCard.getContext().setExitScore(exit);
        }
        return batterCard.getContext();
    }

    public ControlMetrics updateControlMetricsOfBatter(BatterCard batterCard, Event event){
        if(batterCard.getControl() == null){
            ControlMetrics controlMetrics = ControlMetrics.builder()
                    .dots(null)
                    .singles(null)
                    .doubles(null)
                    .boundaries(null)
                    .boundaryPercentage(null)
                    .build();
            batterCard.setControl(controlMetrics);
        }
        switch (event.getEventType()){
            case EventType.DOT_BALL :
                int dots = batterCard.getControl().getDots() == null ? 0 : batterCard.getControl().getDots();
                batterCard.getControl().setDots(dots+1);
                break;
            case EventType.ONE :
                int singles = batterCard.getControl().getSingles() == null ? 0 : batterCard.getControl().getSingles();
                batterCard.getControl().setSingles(singles+1);
                break;
            case EventType.TWO:
                int doubles = batterCard.getControl().getDoubles() == null ? 0 : batterCard.getControl().getDoubles();
                batterCard.getControl().setDoubles(doubles+1);
                break;
            case EventType.FOUR: case EventType.SIX:
                int boundaries = batterCard.getControl().getBoundaries() == null ? 0 : batterCard.getControl().getBoundaries();
                batterCard.getControl().setBoundaries(boundaries+1);
                break;
        }
        int fours = batterCard.getScoring().getFours() == null ? 0 : batterCard.getScoring().getFours();
        int sixes = batterCard.getScoring().getSixes() == null ? 0 : batterCard.getScoring().getSixes();
        int runs = batterCard.getScoring().getRuns() == null ? 0 : batterCard.getScoring().getRuns();
        int boundaries = batterCard.getControl().getBoundaries() == null ? 0 : batterCard.getControl().getBoundaries();

        if(runs>0 && boundaries>0){
            double boundaryPercentage = ((fours * 4) + (sixes * 6)) * 100.0 / runs;
            batterCard.getControl().setBoundaryPercentage(boundaryPercentage);
        }
        return batterCard.getControl();
    }

    public PhaseBreakdownOfPlayer updatePhaseBreakdownOfBatter(MatchState matchState, BatterCard batterCard, Event event){
        ScoreSummary scoreSummary = matchState.getScoreCard().getInnings().get(matchState.getCurrentInningNumber()-1).getScoreSummary();
        if(batterCard.getPhases() == null){
            PhaseBreakdownOfPlayer phaseBreakDown = PhaseBreakdownOfPlayer.builder()
                    .powerPlayRuns(null)
                    .middleOverRuns(null)
                    .deathOverRuns(null)
                    .build();
            batterCard.setPhases(phaseBreakDown);
        }
        int totalLegalBallsThisInning = Util.overBallsToBalls(scoreSummary.getOvers(), scoreSummary.getBalls());
        if(event.getRunOffBat() != null){
            if(totalLegalBallsThisInning<=36){
                int runs = batterCard.getPhases().getPowerPlayRuns() == null ? 0 : batterCard.getPhases().getPowerPlayRuns();
                batterCard.getPhases().setPowerPlayRuns(runs+event.getRunOffBat());
            }
            if(totalLegalBallsThisInning>36 && totalLegalBallsThisInning<=90){
                int runs = batterCard.getPhases().getMiddleOverRuns() == null ? 0 : batterCard.getPhases().getMiddleOverRuns();
                batterCard.getPhases().setMiddleOverRuns(runs+event.getRunOffBat());
            }
            if(totalLegalBallsThisInning>90){
                int runs = batterCard.getPhases().getDeathOverRuns() == null ? 0 : batterCard.getPhases().getDeathOverRuns();
                batterCard.getPhases().setDeathOverRuns(runs+event.getRunOffBat());
            }
        }
        return batterCard.getPhases();
    }
    public ScoringStats updateScoringStatsOfBatter(BatterCard batterCard, Event event){
        if(batterCard.getScoring() == null){
            ScoringStats scoringStats = ScoringStats.builder()
                    .runs(null)
                    .balls(null)
                    .fours(null)
                    .sixes(null)
                    .strikeRate(null)
                    .build();
            batterCard.setScoring(scoringStats);
        }
        if(event.getRunOffBat() != null && event.getRunOffBat() > 0){
            updateBatterRuns(batterCard, event.getRunOffBat());
        }
        if (event.getExtraType() != ExtraType.WIDE && !(event.getExtraType() == ExtraType.NO_BALL && event.getRunOffBat() == null)) {
            updateBatterBalls(batterCard);
        }
        switch (event.getEventType()) {
            case EventType.SIX:
                updateBatterSixes(batterCard);
                break;
            case EventType.FOUR:
                updateBatterFours(batterCard);
                break;
        }
        updateBatterStrikeRate(batterCard);
        return batterCard.getScoring();
    }
    public DismissalInfo updateDismissalOfBatter(MatchState matchState, BowlerCard bowlerCard ,BatterCard batterCard, Event event){
        if(batterCard.getDismissal().getStatus() != BattingStatus.NOT_OUT){
            throw new RuntimeConflictException("This batter is not batting");
        }
        Inning inning = Util.getCurrentInning(matchState);
        batterCard.getDismissal().setStatus(BattingStatus.OUT);
        batterCard.getDismissal().setDismissalType(event.getDismissedType());
        batterCard.getDismissal().setBowlerId(inning.getCurrentBowlerId());
        batterCard.getDismissal().setFielderId(event.getFielderId());
        String dismissalText ="";
        if(event.getDismissedType() == BOWLED){
            dismissalText+="b "+bowlerCard.getBowler().getPlayerName();
        } else if (event.getDismissedType()==CAUGHT_AND_BOWLED) {
            dismissalText +="c & b "+bowlerCard.getBowler().getPlayerName();
        }else if(event.getDismissedType()==CAUGHT){
            Player fielder = playerService.getPlayerById(event.getFielderId());
            dismissalText +="c "+fielder.getShortName()+" b "+bowlerCard.getBowler().getPlayerName();
        } else if (event.getDismissedType()==LBW) {
            dismissalText +="lbw "+bowlerCard.getBowler().getPlayerName();
        } else if (event.getDismissedType()==RUN_OUT) {
            Player fielder = playerService.getPlayerById(event.getFielderId());
            dismissalText +="run out "+fielder.getShortName();
        } else if(event.getDismissedType()==STUMPED){
            Player fielder = playerService.getPlayerById(event.getFielderId());
            dismissalText +="st "+fielder.getShortName()+" b "+bowlerCard.getBowler().getPlayerName();
        }else{
            dismissalText+=event.getDismissedType();
        }
        batterCard.getDismissal().setDismissalText(dismissalText);
        batterCard.setOnStrike(false);
        String exitScore = inning.getScoreSummary().getRuns() +"-"+inning.getScoreSummary().getWickets();
        updateContextMetricsOfBatter(batterCard,event,batterCard.getContext().getEntryScore(), exitScore);
        return batterCard.getDismissal();
    }

    public PartnershipCard createPartnershipCard(Long playerId, MatchState matchState, Inning inning, Event event){
        PartnershipCard partnershipCard = inning.getPartnershipCard();


        // CASE 1: second opener selected -> create first partnership
        if(inning.getStrikerId() != null && inning.getNonStrikerId() == null && partnershipCard.getPartnerships().isEmpty()){
            List<PartnershipContribution> contributions = new ArrayList<>();
            Player strikerPlayer = playerService.getPlayerById(inning.getStrikerId());
            Player nonStrikerPlayer = playerService.getPlayerById(playerId);
            PartnershipContribution striker = PartnershipContribution.builder()
                    .playerId(inning.getStrikerId())
                    .name(strikerPlayer.getFullName())
                    .runs(0)
                    .balls(0)
                    .fours(0)
                    .sixes(0)
                    .build();
            PartnershipContribution nonStriker = PartnershipContribution.builder()
                    .playerId(playerId)
                    .name(nonStrikerPlayer.getFullName())
                    .runs(0)
                    .balls(0)
                    .fours(0)
                    .sixes(0)
                    .build();
            contributions.add(striker);
            contributions.add(nonStriker);

            Partnership openingPartnership = Partnership.builder()
                            .wicket(1)
                            .partnershipRuns(0)
                            .partnershipBalls(0)
                            .isActive(true)
                            .contributions(contributions)
                            .build();

            partnershipCard.getPartnerships().add(openingPartnership);
        }
        // CASE 2: new batter after wicket -> close old, create new
        else if(inning.getStrikerId()==null || inning.getNonStrikerId()==null){

            Partnership activePartnership = partnershipCard.getPartnerships()
                            .stream()
                            .filter(Partnership::getIsActive)
                            .findFirst()
                            .orElse(null);

            if(activePartnership != null){
                activePartnership.setIsActive(false);

                Long survivingBatter = inning.getStrikerId() != null ? inning.getStrikerId() : inning.getNonStrikerId();

                BatterCard strikePlayer=null;
                BatterCard nonStrikePlayer=null;

                for(BatterCard bc : inning.getBattingCard().getBatters()){
                    if(bc.getBatter().getPlayerId().equals(survivingBatter)){
                        strikePlayer = bc;
                    }
                    if(bc.getBatter().getPlayerId().equals(playerId)){
                        nonStrikePlayer = bc;
                    }
                }
                Partnership newPartnership = Partnership.builder()
                                .wicket(inning.getScoreSummary().getWickets()+1)
                                .partnershipRuns(0)
                                .partnershipBalls(0)
                                .isActive(true)
                                .contributions(new ArrayList<>(List.of(
                                                        PartnershipContribution.builder()
                                                                .playerId(survivingBatter)
                                                                .name(strikePlayer.getBatter().getPlayerName())
                                                                .runs(0)
                                                                .balls(0)
                                                                .fours(0)
                                                                .sixes(0)
                                                                .build(),
                                                        PartnershipContribution.builder()
                                                                .playerId(playerId)
                                                                .name(nonStrikePlayer.getBatter().getPlayerName())
                                                                .runs(0)
                                                                .balls(0)
                                                                .fours(0)
                                                                .sixes(0)
                                                                .build()
                                                )
                                        )
                                )
                                .build();

                partnershipCard.getPartnerships().add(newPartnership);
            }
        }
        return partnershipCard;
    }
    public void updatePartnership(MatchState matchState, Inning inning, Event event){

        Partnership p = inning.getPartnershipCard().getPartnerships()
                .stream()
                .filter(Partnership::getIsActive) .findFirst()
                .orElseThrow(()-> new ResourceNotFoundException("There is no current partnerhship"));


        // all team runs count in partnership
        int runs = 0;

        if(event.getRunOffBat()!=null) runs += event.getRunOffBat();

        if(event.getExtrasRuns()!=null) runs += event.getExtrasRuns();

        if(event.getSubExtrasRuns()!=null) runs += event.getSubExtrasRuns();


        p.setPartnershipRuns(p.getPartnershipRuns()+runs);


        boolean legal = event.getEventType() != EventType.WIDE && event.getEventType() != EventType.NO_BALL;

        if(legal){
            p.setPartnershipBalls(p.getPartnershipBalls()+1);
        }



        // striker contribution
        PartnershipContribution striker = p.getContributions()
                        .stream()
                        .filter(c -> c.getPlayerId().equals(inning.getStrikerId()))
                        .findFirst()
                        .orElseThrow(()-> new ResourceNotFoundException("Striker contribution not found"));


        if(striker != null){
            int batRuns = event.getRunOffBat()==null ? 0 : event.getRunOffBat();

            striker.setRuns(striker.getRuns()+batRuns);

            if(legal){
                striker.setBalls(striker.getBalls()+1);
            }

            if(batRuns==4){
                striker.setFours(striker.getFours()+1);
            }

            if(batRuns==6){
                striker.setSixes( striker.getSixes()+1);
            }
        }
    }

    public OverProgression createOverProgression(MatchState matchState, Inning inning, Event event){

        OverProgression overProgression = inning.getOverProgression();

        // initialize list if null
        if(overProgression.getOvers() == null){
            overProgression.setOvers(
                    new ArrayList<>()
            );
        }
        List<OverSummary> overSummaries = overProgression.getOvers();

        int oversCompleted = inning.getScoreSummary().getOvers();
        int balls = inning.getScoreSummary().getBalls();
        int totalBalls = Util.overBallsToBalls(oversCompleted, balls);

        boolean legal = event.getEventType() != EventType.WIDE && event.getEventType() != EventType.NO_BALL;

        int currentOverNumber;

        if(legal){
            currentOverNumber = ((totalBalls - 1)/6)+1;
        }else{
            currentOverNumber = (totalBalls/6)+1;
        }
        if(overSummaries.isEmpty() || overSummaries.size() <= currentOverNumber){

            overSummaries.add(
                    OverSummary.builder()
                            .overNumber(currentOverNumber)
                            .runs(0)
                            .wickets(0)
                            .build()
            );
        }
        return overProgression;
    }

    public OverProgression updateOverProgression(MatchState matchState, Inning inning, Event event){
        int totalBalls = Util.overBallsToBalls(inning.getScoreSummary().getOvers(), inning.getScoreSummary().getBalls());
        boolean legal = event.getEventType() != EventType.WIDE && event.getEventType() != EventType.NO_BALL;
        int currentOverNumber;

        if(legal){
            currentOverNumber = ((totalBalls - 1)/6)+1;
        }else{
            currentOverNumber = (totalBalls/6)+1;
        }
        List<OverSummary> overSummaries = inning.getOverProgression().getOvers();
        OverSummary currentOver = overSummaries.get(currentOverNumber-1);
        currentOver.setOverNumber(currentOverNumber);
        int ballRuns = 0;

        if(event.getRunOffBat()!=null)
            ballRuns += event.getRunOffBat();

        if(event.getExtrasRuns()!=null)
            ballRuns += event.getExtrasRuns();

        if(event.getSubExtrasRuns()!=null)
            ballRuns += event.getSubExtrasRuns();

        currentOver.setRuns(currentOver.getRuns()+ballRuns);


        if(event.getIsWicket() != null && Boolean.TRUE.equals(event.getIsWicket())){
            currentOver.setWickets(currentOver.getWickets()+1
            );
        }
        return inning.getOverProgression();
    }

    @Override
    public PhaseBreakDownTeam updatePhaseBreakDown(MatchState matchState, Inning inning, Event event) {
        PhaseBreakDownTeam phaseBreakDownTeam = inning.getPhaseBreakdown();
        if(inning.getPhaseBreakdown() == null){
            PhaseStats phaseStats = PhaseStats.builder()
                    .runs(0)
                    .wickets(0)
                    .runRate(0.0)
                    .build();
            phaseBreakDownTeam = PhaseBreakDownTeam.builder()
                    .powerPlay(phaseStats)
                    .middleOvers(phaseStats)
                    .deathOvers(phaseStats)
                    .build();
            inning.setPhaseBreakdown(phaseBreakDownTeam);
        }
        if(inning.getScoreSummary().getOvers()>=0 && inning.getScoreSummary().getOvers()<=5){
            PhaseStats phaseStats = createPhaseBreakDownTeam(matchState, inning, event);
            phaseBreakDownTeam.setPowerPlay(phaseStats);
        }else if(inning.getScoreSummary().getOvers()>=6 && inning.getScoreSummary().getOvers()<=14){
            PhaseStats phaseStats = createPhaseBreakDownTeam(matchState, inning, event);
            phaseBreakDownTeam.setMiddleOvers(phaseStats);
        }else{
            PhaseStats phaseStats = createPhaseBreakDownTeam(matchState, inning, event);
            phaseBreakDownTeam.setDeathOvers(phaseStats);
        }
        return phaseBreakDownTeam;
    }

    @Override
    public List<FallOfWicket> updateFOW(Long playerId, MatchState matchState, Inning inning, Event event) {
        List<FallOfWicket> fallOfWickets = inning.getFallOfWickets();
        Player player = playerService.getPlayerById(playerId);
        FallOfWicket fallOfWicket = FallOfWicket.builder()
                .scoreAtFall(inning.getScoreSummary().getRuns())
                .over(Util.overBalls(Util.overBallsToBalls(inning.getScoreSummary().getOvers(), inning.getScoreSummary().getBalls())))
                .wicketNumber(inning.getScoreSummary().getWickets())
                .batterId(playerId)
                .batterName(player.getFullName())
                .build();
        fallOfWickets.add(fallOfWicket);
        return fallOfWickets;
    }

    @Override
    public InningControlMetrices updateInningControlMetrics(MatchState matchState, Inning inning, Event event) {

        InningControlMetrices controlMetrics = inning.getControlMetrics();

        // Current values
        int dots = controlMetrics.getDots() == null ? 0 : controlMetrics.getDots();
        int singles = controlMetrics.getSingles() == null ? 0 : controlMetrics.getSingles();
        int doubles = controlMetrics.getDoubles() == null ? 0 : controlMetrics.getDoubles();
        int threes = controlMetrics.getThrees() == null ? 0 : controlMetrics.getThrees();
        int fours = controlMetrics.getFours() == null ? 0 : controlMetrics.getFours();
        int sixes = controlMetrics.getSixes() == null ? 0 : controlMetrics.getSixes();
        int boundaries = controlMetrics.getBoundaries() == null ? 0 : controlMetrics.getBoundaries();

        // Handle event
        switch (event.getEventType()) {

            case DOT_BALL -> controlMetrics.setDots(dots + 1);

            case ONE -> controlMetrics.setSingles(singles + 1);

            case TWO -> controlMetrics.setDoubles(doubles + 1);

            case THREE -> controlMetrics.setThrees(threes + 1);

            case FOUR -> {
                controlMetrics.setFours(fours + 1);
                controlMetrics.setBoundaries(boundaries + 1);
            }

            case SIX -> {
                controlMetrics.setSixes(sixes + 1);
                controlMetrics.setBoundaries(boundaries + 1);
            }

            case ANY_BALL -> {
                Integer runsOffBat = event.getRunOffBat();

                if (runsOffBat != null) {
                    switch (runsOffBat) {
                        case 0 -> controlMetrics.setDots(dots + 1);

                        case 1 -> controlMetrics.setSingles(singles + 1);

                        case 2 -> controlMetrics.setDoubles(doubles + 1);

                        case 3 -> controlMetrics.setThrees(threes + 1);

                        case 4 -> {
                            // Counts as a four even on a no-ball
                            controlMetrics.setFours(fours + 1);
                            controlMetrics.setBoundaries(boundaries + 1);
                        }

                        case 6 -> {
                            // Counts as a six even on a no-ball
                            controlMetrics.setSixes(sixes + 1);
                            controlMetrics.setBoundaries(boundaries + 1);
                        }
                    }
                }
            }
        }

        // Recalculate latest values after updates
        int updatedFours = controlMetrics.getFours() == null ? 0 : controlMetrics.getFours();

        int updatedSixes = controlMetrics.getSixes() == null ? 0 : controlMetrics.getSixes();

        // Total runs scored by boundaries
        int boundaryRuns = (updatedFours * 4) + (updatedSixes * 6);

        // Total team runs in this innings
        int totalRuns =inning.getScoreSummary() != null && inning.getScoreSummary().getRuns() != null ? inning.getScoreSummary().getRuns() : 0;

        // Boundary Percentage = (Runs scored in boundaries / Total runs) × 100
        double boundaryPercentage =totalRuns > 0 ? Math.round(((double) boundaryRuns * 10000) / totalRuns) / 100.0 : 0.0;

        controlMetrics.setBoundaryPercentage(boundaryPercentage);

        return controlMetrics;
    }

    private PhaseStats createPhaseBreakDownTeam(MatchState matchState, Inning inning, Event event){
        int runs;
        int wickets;
        int balls;
        int totalBalls = Util.overBallsToBalls(inning.getScoreSummary().getOvers(), inning.getScoreSummary().getBalls());
        if(inning.getScoreSummary().getOvers()>=0 && inning.getScoreSummary().getOvers()<=5){
            runs = inning.getPhaseBreakdown().getPowerPlay().getRuns();
            wickets = inning.getPhaseBreakdown().getPowerPlay().getWickets();
            balls = Math.min(totalBalls, 36);
        }else if(inning.getScoreSummary().getOvers()>=6 && inning.getScoreSummary().getOvers()<=14){
            runs = inning.getPhaseBreakdown().getMiddleOvers().getRuns();
            wickets = inning.getPhaseBreakdown().getMiddleOvers().getWickets();
            balls = Math.min(totalBalls-36, 54);
        }else{
            runs = inning.getPhaseBreakdown().getDeathOvers().getRuns();
            wickets = inning.getPhaseBreakdown().getDeathOvers().getWickets();
            balls = Math.min(totalBalls-90, 30);
        }
        if(event.getRunOffBat() != null && event.getRunOffBat() > 0){
            runs += event.getRunOffBat();
        }
        if(event.getExtrasRuns() != null && event.getExtrasRuns() > 0){
            runs += event.getExtrasRuns();
        }
        if(event.getSubExtrasRuns() != null && event.getSubExtrasRuns() > 0){
            runs += event.getSubExtrasRuns();
        }
        if(event.getIsWicket() != null && Boolean.TRUE.equals(event.getIsWicket())){
            wickets+=1;
        }
        PhaseStats phaseStats = PhaseStats.builder()
                .runs(runs)
                .wickets(wickets)
                .runRate(Util.calculateRunRate(runs, balls))
                .build();
        return phaseStats;
    }

    private void updateBatterRuns(BatterCard batterCard, int runsOfBat){
        int prevRuns = batterCard.getScoring().getRuns() == null ? 0 : batterCard.getScoring().getRuns();
        batterCard.getScoring().setRuns(prevRuns+runsOfBat);
    }
    private void updateBatterBalls(BatterCard batterCard){
        int prevBalls = batterCard.getScoring().getBalls() == null ? 0 : batterCard.getScoring().getBalls();
        batterCard.getScoring().setBalls(prevBalls+1);
    }
    private void updateBatterFours(BatterCard batterCard){
        int prevFours = batterCard.getScoring().getFours() == null ? 0 : batterCard.getScoring().getFours();
        batterCard.getScoring().setFours(prevFours+1);
    }
    private void updateBatterSixes(BatterCard batterCard){
        int prevSixes = batterCard.getScoring().getSixes() == null ? 0 : batterCard.getScoring().getSixes();
        batterCard.getScoring().setSixes(prevSixes+1);
    }
    private void updateBatterStrikeRate(BatterCard batterCard){
        int runs = batterCard.getScoring().getRuns() == null ? 0 : batterCard.getScoring().getRuns();
        int balls = batterCard.getScoring().getBalls() == null ? 0 : batterCard.getScoring().getBalls();
        if(runs > 0 && balls > 0){
           batterCard.getScoring().setStrikeRate(Util.calculateStrikeRate(runs, balls));
        }
    }
}
