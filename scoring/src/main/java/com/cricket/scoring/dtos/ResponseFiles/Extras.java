package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Extras {
    private Integer wides;
    private Integer noBalls;
    private Integer byes;
    private Integer legByes;
    private Integer penalty;
    private Integer total;
}
