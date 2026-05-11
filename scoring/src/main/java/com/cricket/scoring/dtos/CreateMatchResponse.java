package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.Player;
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
public class CreateMatchResponse {
    private Long id;
    private String externalMatchId; // optional feed id
    private String matchName; // India vs Australia
    private MatchFormat format; // T20 ODI TEST
    private MatchStatus status;
    // SCHEDULED, LIVE, INNINGS_BREAK, COMPLETED

    private String competition; // IPL
    private String season; // 2026
    private Integer matchNumber;

    private LocalDate matchDate;
    private LocalDateTime startTime;

    private Integer totalOvers;
    private Integer ballsPerOver;

    // Toss
    private Long tossWinner;
    private TossDecision tossDecision;
    // BAT / BOWL

    // Rules
    private Boolean drsEnabled;
    private Integer reviewsPerTeam;
    private Boolean superOverEnabled;
    private Boolean dlsEnabled;

    // Powerplay config
    private Integer powerplayStartOver;
    private Integer powerplayEndOver;

    // current match state
    private Integer currentInnings;
    private Integer currentOver;

    // Result
    private String resultText;
    private String winnerTeamName;

    private VenueDTO venue;
    private List<PlayerDTO> homePlaying11 = new ArrayList<>();
    private List<PlayerDTO> homeSubstitutes = new ArrayList<>();
    private List<PlayerDTO> awayPlaying11 = new ArrayList<>();
    private List<PlayerDTO> awaySubstitutes = new ArrayList<>();
    private TeamDTO homeTeam;
    private TeamDTO awayTeam;

    // audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
