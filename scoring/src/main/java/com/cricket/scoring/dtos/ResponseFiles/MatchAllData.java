package com.cricket.scoring.dtos.ResponseFiles;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchAllData {
    private SetupFile setupFile;
    private MatchState matchState;
    private EventFile eventFile;
}
