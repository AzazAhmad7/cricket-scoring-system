package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.MatchSquad;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.enums.BattingStyle;
import com.cricket.scoring.entities.enums.BowlingStyle;
import com.cricket.scoring.entities.enums.PlayerRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerDTO {
    private Long id;
    private String fullName;
    private String shortName;

    private PlayerRole role;

    private BattingStyle battingStyle;
    private BowlingStyle bowlingStyle;

    private Boolean captain;
    private Boolean wicketKeeper;
    private Integer battingOrder;
    private Boolean active;

    private Integer matchesPlayed;
    private Integer runs;
    private Integer wickets;
}
