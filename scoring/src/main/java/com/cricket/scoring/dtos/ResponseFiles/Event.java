package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.entities.enums.DismissalType;
import com.cricket.scoring.entities.enums.EventType;
import com.cricket.scoring.entities.enums.ExtraType;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    private Integer eventNumber;
    private Long matchId;
    private EventType eventType;
    private Integer inningNumber;
    private Integer oversCompleted;
    private Integer ballInOver;
    private Integer totalLegalBallsBowled;
    private Long playerId;
    private Long strikerId;
    private Long nonStrikerId;
    private Long bowlerId;
    private Integer runOffBat;
    private Integer extrasRuns;
    private Integer subExtrasRuns;
    private ExtraType extraType;
    private ExtraType subExtraType;
    private Boolean isLegalDelivery;
    private Boolean isFreeHit;
    private Boolean isWicket;
    private Long dismissedPlayerId;
    private Long fielderId;
    private DismissalType dismissedType;
    private String dismissalText;
    private String ballOutCome;
}
