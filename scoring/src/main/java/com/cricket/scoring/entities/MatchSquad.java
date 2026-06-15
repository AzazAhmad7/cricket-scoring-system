package com.cricket.scoring.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "match_squad",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_match_player",
                        columnNames = {"match_id", "player_id"}
                )
        },
        indexes = {
                @Index(name = "idx_ms_match", columnList = "match_id"),
                @Index(name = "idx_ms_team", columnList = "team_id"),
                @Index(name = "idx_ms_player", columnList = "player_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSquad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Match
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "match_id",
            nullable = false
    )
    @JsonIgnore
    private Match match;

    /*
     * Player
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "player_id",
            nullable = false
    )
    private Player player;

    /*
     * Team
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "team_id",
            nullable = false
    )
    private Team team;

    /*
     * Squad Information
     */
    @Builder.Default
    private Boolean playingXI = false;

    @Builder.Default
    private Boolean captain = false;

    @Builder.Default
    private Boolean wicketKeeper = false;

    @Builder.Default
    private Boolean substitutePlayer = false;

    @Builder.Default
    private Boolean impactPlayer = false;

    private Integer battingOrder;

    /*
     * Match Statistics
     */
    @Builder.Default
    private Boolean dismissed = false;

    @Builder.Default
    private Integer runsScored = 0;

    @Builder.Default
    private Integer ballsFaced = 0;

    @Builder.Default
    private Integer wicketsTaken = 0;

    /*
     * Audit
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}