package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.Venue;
import com.cricket.scoring.entities.enums.MatchFormat;
import com.cricket.scoring.entities.enums.MatchStatus;
import com.cricket.scoring.entities.enums.TossDecision;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchDTO {
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
    private TeamDTO homeTeam;
    private TeamDTO awayTeam;
    private VenueDTO venue;

    // Toss
    private TeamDTO tossWinner;
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

    // audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
