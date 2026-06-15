package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.TournamentTeam;
import com.cricket.scoring.entities.enums.TournamentStatus;
import com.cricket.scoring.entities.enums.TournamentType;
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
public class CreateTournamentResponse {
    private Long id;

    private String name;

    private String location;
    private TournamentType type;

    private Integer overs;

    private Integer maxTeams;

    private LocalDate startDate;

    private LocalDate endDate;
    private TournamentStatus status;

    private List<MatchDTO> matches = new ArrayList<>();

    // OWNER
    private Long createdByUserId;

    private String logoUrl;

    private Boolean isPublic;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
