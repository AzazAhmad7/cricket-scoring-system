package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BallEvent {
    private String display; // "W", "4", "Lb2"
    private int runsOffBat;
    private int bowlerRuns;
    private boolean wicket;
}
