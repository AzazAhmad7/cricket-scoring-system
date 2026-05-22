package com.cricket.scoring.dtos;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImpactPlayerDTO {
    private Long teamId;
    private Long impactInPlayerId;
    private Long impactOutPlayerId;
}
