package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.Tournament;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentTeamDTO {
    private Long id;
    private Tournament tournament;
    private Team team;
    private Integer seed;
    private String groupName;
}
