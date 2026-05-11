package com.cricket.scoring.dtos.ResponseFiles;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OverEvent {
    private Integer overNumber;
    private List<BallEvent> deliveries = new ArrayList<>();
}
