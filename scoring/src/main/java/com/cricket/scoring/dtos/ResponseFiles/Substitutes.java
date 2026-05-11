package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.dtos.PlayerDTO;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Substitutes {
    private List<PlayerDTO> players;
}
