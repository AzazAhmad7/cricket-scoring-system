package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.enums.TournamentStatus;
import com.cricket.scoring.entities.enums.TournamentType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTournametRequest {

    private String name;

    private String location;
    private TournamentType type;

    private Integer overs;

    private Integer maxTeams;

    private LocalDate startDate;

    private LocalDate endDate;

    private String logoUrl;
}