package com.cricket.scoring.entities;

import com.cricket.scoring.entities.enums.TournamentStatus;
import com.cricket.scoring.entities.enums.TournamentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tournaments",
        indexes = {
                @Index(name = "idx_tournament_name", columnList = "name"),
                @Index(name = "idx_tournament_status", columnList = "status"),
                @Index(name = "idx_tournament_start_date", columnList = "startDate")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentType type;

    private Integer overs;

    private Integer maxTeams;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status;

    /*
     * Owner/User who created tournament
     */
    private Long createdByUserId;

    @Column(length = 500)
    private String logoUrl;

    @Builder.Default
    private Boolean isPublic = true;

    /*
     * Tournament Matches
     */
    @OneToMany(
            mappedBy = "tournament",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<Match> matches = new ArrayList<>();

    /*
     * Tournament Teams
     */
    @OneToMany(
            mappedBy = "tournament",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<TournamentTeam> teams = new ArrayList<>();

    /*
     * Points Table
     */
    @OneToMany(
            mappedBy = "tournament",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<PointsTable> pointsTables = new ArrayList<>();

    /*
     * Batting Stats
     */
    @OneToMany(
            mappedBy = "tournament",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<BatterStats> batterStats = new ArrayList<>();

    /*
     * Bowling Stats
     */
    @OneToMany(
            mappedBy = "tournament",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<BowlerStats> bowlerStats = new ArrayList<>();

    /*
     * Fielding Stats
     */
    @OneToMany(
            mappedBy = "tournament",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<FielderTournamentStats> fieldingStats = new ArrayList<>();

    /*
     * Audit Fields
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}