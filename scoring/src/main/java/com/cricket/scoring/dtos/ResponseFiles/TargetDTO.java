package com.cricket.scoring.dtos.ResponseFiles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores target information for a chase innings.
 *
 * Examples:
 * - Team needs 251 runs to win
 * - Team needs 120 runs in 20 overs
 * - Team needs 45 runs in 30 balls
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetDTO {

    /**
     * Score that must be reached to win.
     * Example:
     * If Team A scores 250, targetRuns = 251
     */
    private Integer targetRuns;

    /**
     * Runs required at the current moment.
     * Example:
     * Target = 251, Current Score = 200
     * runsRemaining = 51
     */
    private Integer runsRemaining;

    /**
     * Wickets remaining in the innings.
     * Example:
     * 10 wickets total, 3 wickets lost
     * wicketsRemaining = 7
     */
    private Integer wicketsRemaining;

    /**
     * Total legal balls available for the innings.
     * Example:
     * T20 = 120, ODI = 300
     */
    private Integer totalBalls;

    /**
     * Balls remaining at the current moment.
     * Example:
     * Total = 300, Bowled = 240
     * ballsRemaining = 60
     */
    private Integer ballsRemaining;

    /**
     * Required run rate.
     * Formula:
     * runsRemaining * 6.0 / ballsRemaining
     */
    private Double requiredRunRate;

    /**
     * Whether the batting team has successfully chased the target.
     */
    private Boolean targetAchieved;
}
