package com.cricket.scoring.entities;

import com.cricket.scoring.entities.enums.TournamentStatus;
import com.cricket.scoring.entities.enums.TournamentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String location;

    @OneToMany(mappedBy = "tournament")
    private List<Match> matches = new ArrayList<>();

    @OneToMany(mappedBy = "tournament")
    private List<TournamentTeam> teams = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private TournamentType type;

    private Integer overs;

    private Integer maxTeams;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private TournamentStatus status;

    // OWNER
    private Long createdByUserId;

    private String logoUrl;

    private Boolean isPublic;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
