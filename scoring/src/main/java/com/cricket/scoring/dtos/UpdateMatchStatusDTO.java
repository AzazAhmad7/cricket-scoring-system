package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.enums.MatchStatus;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateMatchStatusDTO {
    private MatchStatus matchStatus;
}
