package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.entities.Player;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Team {
    private Long id;
    private String name;
    private List<Player> players;
}
