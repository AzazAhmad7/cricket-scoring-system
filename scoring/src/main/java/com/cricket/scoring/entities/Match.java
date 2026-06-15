package com.cricket.scoring.entities;

import com.cricket.scoring.entities.enums.MatchFormat;
import com.cricket.scoring.entities.enums.MatchStatus;
import com.cricket.scoring.entities.enums.TossDecision;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "matches",
        indexes = {
                @Index(name = "idx_match_tournament", columnList = "tournament_id"),
                @Index(name = "idx_match_home_team", columnList = "home_team_id"),
                @Index(name = "idx_match_away_team", columnList = "away_team_id"),
                @Index(name = "idx_match_status", columnList = "status"),
                @Index(name = "idx_match_date", columnList = "match_date")
        }
)
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String externalMatchId;

    @Column(nullable = false)
    private String matchName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    @JsonIgnore
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    private Long competition;

    private String season;

    private Integer matchNumber;

    private LocalDate matchDate;

    private LocalDateTime startTime;

    private Integer totalOvers;

    @Builder.Default
    private Integer ballsPerOver = 6;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toss_winner_team_id")
    private Team tossWinner;

    @Enumerated(EnumType.STRING)
    private TossDecision tossDecision;

    // Rules
    @Builder.Default
    private Boolean drsEnabled = false;

    @Builder.Default
    private Integer reviewsPerTeam = 0;

    @Builder.Default
    private Boolean superOverEnabled = false;

    @Builder.Default
    private Boolean dlsEnabled = false;

    @Builder.Default
    private Boolean impactPlayerEnabled = false;

    // Powerplay
    private Integer powerplayStartOver;

    private Integer powerplayEndOver;

    // Current Match State
    @Builder.Default
    private Integer currentInnings = 1;

    @Builder.Default
    private Integer currentOver = 0;

    // Result
    @Column(length = 500)
    private String resultText;

    private String winnerTeamName;

    /*
     * Match Squads
     */
    @OneToMany(
            mappedBy = "match",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<MatchSquad> squads = new ArrayList<>();

    /*
     * Audit Fields
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}