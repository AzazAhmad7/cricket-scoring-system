package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Teams {
    private Long battingTeamId;
    private Team homeTeam;
    private Team awayTeam;
}
