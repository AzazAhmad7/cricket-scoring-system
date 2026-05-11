package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.Match;
import com.cricket.scoring.entities.Player;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamDTO {
    private Long id;
    private String name; // India

    private String shortName; // IND

    private String nickname;

    private String country;

    private String logoUrl;

    private String primaryColor;

    private String coachName;

    private Integer ranking;

    private Boolean active;
    private List<PlayerDTO> players;
}
