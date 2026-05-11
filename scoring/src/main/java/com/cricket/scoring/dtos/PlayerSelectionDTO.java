package com.cricket.scoring.dtos;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerSelectionDTO {
    private Long playerId;

    private Boolean playingXI;

    private Boolean captain;

    private Boolean wicketKeeper;

    private Boolean substitutePlayer;

    private Integer battingOrder;
}
