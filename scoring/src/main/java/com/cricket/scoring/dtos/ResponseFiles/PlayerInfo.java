package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerInfo {
    private Long playerId;
    private String playerName;
    private Integer battingPosition;
}
