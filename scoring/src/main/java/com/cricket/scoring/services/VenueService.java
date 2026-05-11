package com.cricket.scoring.services;

import com.cricket.scoring.dtos.VenueDTO;
import com.cricket.scoring.entities.Venue;

import java.util.List;

public interface VenueService {
    public VenueDTO createVenue(VenueDTO venueDTO);
    public List<VenueDTO> getAllVenues();
    public VenueDTO getVenueById(Long id);
    public VenueDTO updateVenue(Long venueId, VenueDTO venueDTO);
}
