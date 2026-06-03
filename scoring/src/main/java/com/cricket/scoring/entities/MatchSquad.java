package com.cricket.scoring.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="match_squad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSquad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne
    @JoinColumn(name="team_id")
    private Team team;

    private Boolean playingXI;

    private Boolean captain;

    private Boolean wicketKeeper;

    private Boolean substitutePlayer;

    private Integer battingOrder;

    private Boolean impactPlayer;

    private Boolean dismissed;


    private Integer runsScored;

    private Integer ballsFaced;

    private Integer wicketsTaken;


    private LocalDateTime createdAt;
}
