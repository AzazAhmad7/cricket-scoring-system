package com.cricket.scoring.dtos;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SquadSelectionRequest {
    private List<PlayerSelectionDTO> homeSquad;

    private List<PlayerSelectionDTO> awaySquad;
}
