package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartnershipContribution {
    private Long playerId;
    private String name;
    private Integer runs;
    private Integer balls;
    private Integer fours;
    private Integer sixes;
}
