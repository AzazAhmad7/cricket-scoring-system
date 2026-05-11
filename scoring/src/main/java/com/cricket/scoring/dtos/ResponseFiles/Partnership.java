package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Partnership {
    private Integer wicket;
    private Integer partnershipRuns;
    private Integer partnershipBalls;
    private Boolean isActive;
    private List<PartnershipContribution> contributions = new ArrayList<>();
}
