package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreSummary {
    private Integer runs;
    private Integer wickets;
    private Integer overs;
    private Integer balls;
    private Double runRate;
    private Integer declared;
}
