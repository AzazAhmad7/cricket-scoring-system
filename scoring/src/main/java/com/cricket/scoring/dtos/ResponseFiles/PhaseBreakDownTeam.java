package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhaseBreakDownTeam {
    private PhaseStats powerPlay;
    private PhaseStats middleOvers;
    private PhaseStats deathOvers;
}
