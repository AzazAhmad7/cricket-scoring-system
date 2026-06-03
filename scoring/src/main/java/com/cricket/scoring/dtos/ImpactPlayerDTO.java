package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.enums.EventType;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImpactPlayerDTO {
    private EventType eventType;
    private Boolean isImpact;
    private Long teamId;
    private Long impactInPlayerId;
    private Long impactOutPlayerId;
}
