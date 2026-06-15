package com.cricket.scoring.entities;

import com.cricket.scoring.entities.enums.BattingStyle;
import com.cricket.scoring.entities.enums.BowlingStyle;
import com.cricket.scoring.entities.enums.PlayerRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "players",
        indexes = {
                @Index(name = "idx_player_team", columnList = "team_id"),
                @Index(name = "idx_player_name", columnList = "fullName"),
                @Index(name = "idx_player_role", columnList = "role"),
                @Index(name = "idx_player_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String externalPlayerId;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 50)
    private String shortName;

    private Integer jerseyNumber;

    private String photoUrl;

    private LocalDate dateOfBirth;

    @Column(length = 50)
    private String nationality;

    /*
     * BATTER, BOWLER, ALL_ROUNDER, WICKET_KEEPER
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRole role;

    @Enumerated(EnumType.STRING)
    private BattingStyle battingStyle;

    @Enumerated(EnumType.STRING)
    private BowlingStyle bowlingStyle;

    @Builder.Default
    private Boolean captain = false;

    @Builder.Default
    private Boolean wicketKeeper = false;

    private Integer battingOrder;

    @Builder.Default
    private Boolean active = true;

    /*
     * Current Team
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;

    /*
     * Match Squads
     */
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<MatchSquad> matchSquads = new ArrayList<>();

    /*
     * Tournament Batting Stats
     */
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<BatterStats> batterStats = new ArrayList<>();

    /*
     * Tournament Bowling Stats
     */
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<BowlerStats> bowlerStats = new ArrayList<>();

    /*
     * Tournament Fielding Stats
     */
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<FielderTournamentStats> fieldingStats = new ArrayList<>();

    /*
     * Career Statistics
     */
    @Builder.Default
    private Integer matchesPlayed = 0;

    @Builder.Default
    private Integer runs = 0;

    @Builder.Default
    private Integer wickets = 0;

    @Builder.Default
    private Double battingAverage = 0.0;

    @Builder.Default
    private Double bowlingAverage = 0.0;
}