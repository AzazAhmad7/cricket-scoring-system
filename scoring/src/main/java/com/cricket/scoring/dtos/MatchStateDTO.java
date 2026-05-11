package com.cricket.scoring.dtos;

import com.cricket.scoring.MatchStateDTO.*;
import com.cricket.scoring.entities.enums.MatchStatus;
import lombok.Data;

import java.util.List;

@Data
public class MatchStateDTO {

    private Long matchId;

    private MatchStatus matchStatus;

    private Integer inningsNumber;


    // score
    private Integer runs;

    private Integer wickets;

    private Double overs;

    private Double runRate;


    // target info
    private Integer target;

    private Double requiredRunRate;


    private BatterStateDTO striker;

    private BatterStateDTO nonStriker;

    private BowlerStateDTO currentBowler;


    private PartnershipDTO currentPartnership;


    private OverStateDTO currentOver;


    private List<FallOfWicketDTO> fallOfWickets;


    private String battingTeam;

    private String bowlingTeam;
}
