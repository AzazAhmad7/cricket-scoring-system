package com.cricket.scoring.dtos;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamDetailsDTO {
    private Long id;
    private String name;
    private String shortName;
}
