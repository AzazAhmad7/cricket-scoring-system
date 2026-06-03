package com.cricket.scoring.dtos;

import lombok.Data;

import java.util.List;

@Data
public class AddTeamsToTournamentRequest {
    private Long tournamentId;

    private List<Long> teamIds;
}
