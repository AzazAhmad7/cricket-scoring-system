package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.enums.MatchFormat;
import com.cricket.scoring.entities.enums.MatchStatus;
import com.cricket.scoring.entities.enums.TossDecision;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateMatchRequest {
    private Long homeTeamId;

    private Long awayTeamId;

    private Long venueId;

    private String matchName;

    private Long competition;

    private String season;

    private Integer matchNumber;

    private MatchFormat format;

    private Integer totalOvers;

    private Integer ballsPerOver;

    private Long tossWinner;
    private TossDecision tossDecision;

    // Rules
    private Boolean drsEnabled;

    private Integer reviewsPerTeam;

    private Boolean superOverEnabled;

    private Boolean dlsEnabled;

    private Boolean impactPlayerEnabled;


    // Powerplay
    private Integer powerplayStartOver;

    private Integer powerplayEndOver;

    private List<Integer> homePlaying11 = new ArrayList<>();
    private List<Integer> homeSubstitutes = new ArrayList<>();
    private List<Integer> homeBenchPlayers = new ArrayList<>();
    private List<Integer> awayPlaying11 = new ArrayList<>();
    private List<Integer> awaySubstitutes = new ArrayList<>();
    private List<Integer> awayBenchPlayers = new ArrayList<>();


    private LocalDate matchDate;

    private LocalDateTime startTime;
}
