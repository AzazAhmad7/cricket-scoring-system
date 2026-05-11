package com.cricket.scoring.MatchStateDTO;

import lombok.Data;

import java.util.List;

@Data
public class OverStateDTO {

    private Integer overNumber;

    private List<String> deliveries;
}
