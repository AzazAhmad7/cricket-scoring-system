package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BowlingCard {
    private List<BowlerCard> bowlers;

}
