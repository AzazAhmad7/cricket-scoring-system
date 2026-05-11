package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhaseStats {
    private Integer runs;
    private Integer wickets;
    private Double runRate;
}
