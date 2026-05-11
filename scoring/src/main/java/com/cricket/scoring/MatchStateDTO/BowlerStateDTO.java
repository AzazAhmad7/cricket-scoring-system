package com.cricket.scoring.MatchStateDTO;


import lombok.Data;

@Data
public class BowlerStateDTO {

    private Long playerId;

    private String playerName;

    private Double overs;

    private Integer maidens;

    private Integer runsConceded;

    private Integer wickets;

    private Double economy;
}
