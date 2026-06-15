package com.cricket.scoring.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "teams",
        indexes = {
                @Index(name = "idx_team_name", columnList = "name"),
                @Index(name = "idx_team_country", columnList = "country"),
                @Index(name = "idx_team_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String name;

    @Column(length = 10)
    private String shortName;

    @Column(length = 100)
    private String nickname;

    @Column(length = 100)
    private String country;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 20)
    private String primaryColor;

    @Column(length = 100)
    private String coachName;

    private Integer ranking;

    @Builder.Default
    private Boolean active = true;

    /*
     * Tournament Registrations
     */
    @OneToMany(
            mappedBy = "team",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<TournamentTeam> tournaments = new ArrayList<>();

    /*
     * Matches as Home Team
     * No Cascade - deleting a team should not delete matches
     */
    @OneToMany(mappedBy = "homeTeam")
    @JsonIgnore
    @Builder.Default
    private List<Match> homeMatches = new ArrayList<>();

    /*
     * Matches as Away Team
     */
    @OneToMany(mappedBy = "awayTeam")
    @JsonIgnore
    @Builder.Default
    private List<Match> awayMatches = new ArrayList<>();

    /*
     * Players
     * No Cascade - players may transfer to another team
     */
    @OneToMany(mappedBy = "team")
    @JsonIgnore
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    /*
     * Points Table Entries
     */
    @OneToMany(
            mappedBy = "team",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<PointsTable> pointsTableEntries = new ArrayList<>();
}