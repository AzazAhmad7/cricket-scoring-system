package com.cricket.scoring.dtos.ResponseFiles;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rules {
    private Integer overs;
    private Integer ballsPerOver;
    private Integer powerPlayStartOver;
    private Integer powerPlayEndOver;
    private Boolean drsEnabled;
    private Integer reviewPerTeam;
    private Boolean superOverEnabled;
    private Boolean dlsEnabled;
}
