package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.entities.enums.MatchResultType;
import com.cricket.scoring.entities.enums.ResultMarginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores the final outcome of a cricket match.
 *
 * Examples:
 * - India won by 6 wickets
 * - Australia won by 25 runs
 * - Match tied
 * - Match drawn
 * - No result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultDTO {

    /**
     * Overall result type.
     * WIN     -> One team won
     * TIE     -> Scores level and tie rules apply
     * DRAW    -> Match drawn (mainly multi-day matches)
     * NO_RESULT -> Match abandoned / insufficient play
     */
    private MatchResultType resultType;

    /**
     * Winning team ID.
     * Null for TIE, DRAW, and NO_RESULT.
     */
    private Long winningTeamId;

    /**
     * Winning team name for display.
     */
    private String winningTeamName;

    /**
     * Margin type used when there is a winner.
     * RUNS    -> "won by 25 runs"
     * WICKETS -> "won by 6 wickets"
     * INNINGS -> "won by an innings and 45 runs"
     */
    private ResultMarginType marginType;

    /**
     * Numeric value of the winning margin.
     * Examples:
     * 25 -> runs
     * 6  -> wickets
     * 45 -> innings and 45 runs
     */
    private Integer marginValue;

    /**
     * Number of innings by which the team won.
     * Used only for multi-day matches.
     * Example:
     * "won by an innings and 45 runs" => inningsCount = 1
     */
    private Integer inningsCount;

    /**
     * Human-readable result string.
     * Examples:
     * - "India won by 6 wickets"
     * - "Australia won by 25 runs"
     * - "Match tied"
     * - "Match drawn"
     * - "No result"
     */
    private String summary;
}
