package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.enums.TossDecision;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TossRequest {
    private Long tossWinnerTeamId;

    private TossDecision decision;
}
