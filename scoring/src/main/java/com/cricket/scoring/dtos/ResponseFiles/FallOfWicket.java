package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FallOfWicket {
    private Integer wicketNumber;
    private Integer scoreAtFall;
    private Long batterId;
    private String batterName;
    private String over;
}
