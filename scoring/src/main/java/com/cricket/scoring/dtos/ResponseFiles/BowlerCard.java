package com.cricket.scoring.dtos.ResponseFiles;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BowlerCard {
    private PlayerInfo bowler;
    private String overs;
    private Integer maidens;
    private Integer runsConceded;
    private Integer wickets;
    private Double economy;
    private Integer totalLegalDeliveriesBowled;
    private List<OverEvent> eachOver = new ArrayList<>();
    private Integer currentOverRuns;
    private Boolean isCurrentBowler;
}
