package com.cricket.scoring.utils;

import com.cricket.scoring.dtos.ResponseFiles.Inning;
import com.cricket.scoring.dtos.ResponseFiles.MatchState;
import com.cricket.scoring.entities.enums.DismissalType;

import static com.cricket.scoring.entities.enums.DismissalType.*;
import static com.cricket.scoring.entities.enums.DismissalType.CAUGHT_AND_BOWLED;

public class Util {
    public static Inning getCurrentInning(MatchState matchState){
        return matchState.getScoreCard().getInnings()
                .stream().filter(inn -> inn.getInningNumber().equals(matchState.getCurrentInningNumber()))
                .findAny().orElseThrow(()-> new RuntimeException("No inning found"));
    }
    public static String overBalls(Integer ballsBowled){
        int over = ballsBowled / 6;
        int balls = ballsBowled % 6;
        return over+"."+balls;
    }
    public static double calculateRunRate(int runs, int legalBalls){

        if(legalBalls == 0) return 0;

        double overs = legalBalls / 6.0;

        return runs / overs;
    }
    public static double calculateStrikeRate(int runs, int ballsFaced){
        return Math.round(
                ((runs * 100.0) / ballsFaced) * 100
        ) / 100.0;
    }
    public static double calculateEconomy(int runsConceded, int ballsBowled){
        double economy = runsConceded / (ballsBowled / 6.0);
        return Math.round(economy * 100.0) / 100.0;
    }
    public static int overBallsToBalls(int overs, int balls){
        return (overs*6)+balls;
    }
    public static Boolean isBowlerCreditWicket(DismissalType dismissalType){
        return dismissalType == BOWLED
                || dismissalType == CAUGHT
                || dismissalType == LBW
                || dismissalType == STUMPED
                || dismissalType == HIT_WICKET
                || dismissalType == CAUGHT_AND_BOWLED;
    }
}
