package com.cricket.scoring.MatchStateDTO;

import lombok.Data;

@Data
public class BatterStateDTO {

    private Long playerId;

    private String playerName;

    private Integer runs;

    private Integer balls;

    private Integer fours;

    private Integer sixes;

    private Double strikeRate;

    private Boolean onStrike;
}
