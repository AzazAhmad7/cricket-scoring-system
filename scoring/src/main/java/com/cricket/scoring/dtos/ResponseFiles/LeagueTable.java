package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.dtos.PointsTableDTO;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeagueTable {
    private List<PointsTableDTO> leagueTable = new ArrayList<>();
}
