package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetupFile {
    private MatchInfo matchInfo;
    private Teams teams;
    private Squad squads;
    private Toss toss;
    private Rules rules;
    private Venue venue;
    private PlayerStats playerStats;
}
