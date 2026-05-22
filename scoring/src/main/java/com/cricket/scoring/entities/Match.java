package com.cricket.scoring.entities;

import com.cricket.scoring.entities.enums.MatchFormat;
import com.cricket.scoring.entities.enums.MatchStatus;
import com.cricket.scoring.entities.enums.TossDecision;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalMatchId; // optional feed id

    private String matchName; // India vs Australia

    @Enumerated(EnumType.STRING)
    private MatchFormat format; // T20 ODI TEST

    @Enumerated(EnumType.STRING)
    private MatchStatus status;
    // SCHEDULED, LIVE, INNINGS_BREAK, COMPLETED

    private String competition; // IPL
    private String season; // 2026
    private Integer matchNumber;

    private LocalDate matchDate;
    private LocalDateTime startTime;

    private Integer totalOvers;
    private Integer ballsPerOver;

    @ManyToOne
    @JoinColumn(name="home_team_id")
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name="away_team_id")
    private Team awayTeam;

    @ManyToOne
    @JoinColumn(name="venue_id")
    private Venue venue;

    // Toss
    @ManyToOne
    @JoinColumn(name="toss_winner_team_id")
    private Team tossWinner;

    @Enumerated(EnumType.STRING)
    private TossDecision tossDecision;
    // BAT / BOWL

    // Rules
    private Boolean drsEnabled;
    private Integer reviewsPerTeam;
    private Boolean superOverEnabled;
    private Boolean dlsEnabled;
    private Boolean impactPlayerEnabled;

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