package com.cricket.scoring.dtos.ResponseFiles;

import com.cricket.scoring.entities.enums.BattingStatus;
import com.cricket.scoring.entities.enums.DismissalType;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DismissalInfo {
    private BattingStatus status;
    private DismissalType dismissalType;
    private String dismissalText;
    private Long BowlerId;
    private Long fielderId;
}
