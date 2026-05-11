package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ControlMetrics {
    private Integer dots;
    private Integer singles;
    private Integer doubles;
    private Integer boundaries;
    private Double boundaryPercentage;
}
