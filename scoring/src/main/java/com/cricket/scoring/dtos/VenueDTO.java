package com.cricket.scoring.dtos;

import com.cricket.scoring.entities.Match;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VenueDTO {
    private Long id;

    private String name;

    private String city;

    private String state;

    private String country;

    private String pitchType;

    private Integer straightBoundaryMeters;

    private Integer squareBoundaryMeters;

    private Integer capacity;

    private String timeZone;


//    /*
//       one venue hosts many matches
//    */
//    private List<MatchDTO> matches = new ArrayList<>();
}
