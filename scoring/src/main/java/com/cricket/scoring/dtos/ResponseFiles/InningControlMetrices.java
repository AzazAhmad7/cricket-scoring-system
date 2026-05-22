package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InningControlMetrices {
    private Integer dots;
    private Integer singles;
    private Integer doubles;
    private Integer threes;
    private Integer fours;
    private Integer sixes;
    private Integer boundaries;
    private Double boundaryPercentage;
}
