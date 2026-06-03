package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.entities.enums.MatchFormat;
import com.cricket.scoring.entities.enums.MatchStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchInfo {
    private Long matchId;
    private String externalMatchId;
    private String matchName;
    private MatchFormat format;
    private Long competition;
    private String season;
    private Integer matchNumber;
    private MatchStatus status;
    private LocalDate matchDate;
    private LocalDateTime startTime;
}
