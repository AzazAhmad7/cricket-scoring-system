package com.cricket.scoring.controllers;

import com.cricket.scoring.dtos.VenueDTO;
import com.cricket.scoring.entities.Venue;
import com.cricket.scoring.services.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/venues")
public class VenueController {
    private final VenueService venueService;

    @PostMapping("/create")
    public ResponseEntity<VenueDTO> createVenue(@RequestBody VenueDTO venueDTO) {
        return new ResponseEntity<>(venueService.createVenue(venueDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VenueDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }
    @GetMapping("/{id}")
    public ResponseEntity<VenueDTO> getVenueById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }
}
