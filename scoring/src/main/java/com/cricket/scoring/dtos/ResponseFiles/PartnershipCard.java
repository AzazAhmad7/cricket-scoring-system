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
public class PartnershipCard {
    private List<Partnership> partnerships = new ArrayList<>();
}
