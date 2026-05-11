package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.VenueDTO;
import com.cricket.scoring.entities.Venue;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.repositories.VenueRepository;
import com.cricket.scoring.services.VenueService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final ModelMapper modelMapper;

    @Override
    public VenueDTO createVenue(VenueDTO venueDTO) {
        Venue venue = modelMapper.map(venueDTO, Venue.class);
        return modelMapper.map(venueRepository.save(venue), VenueDTO.class);
    }

    @Override
    public List<VenueDTO> getAllVenues() {
        List<Venue> venues = venueRepository.findAll();
        return venues.stream()
                .map((element) -> modelMapper.map(element, VenueDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public VenueDTO getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Venue not found with id: " + id));
        return modelMapper.map(venue, VenueDTO.class);
    }

    @Override
    public VenueDTO updateVenue(Long venueId, VenueDTO venueDTO) {
        Venue venue = venueRepository.findById(venueId).orElseThrow(()->new ResourceNotFoundException("Venue not found with id: " + venueId));
        venue.setName(venueDTO.getName());
        venue.setCity(venueDTO.getCity());
        venue.setState(venueDTO.getState());
        venue.setCountry(venueDTO.getCountry());
        venue.setPitchType(venueDTO.getPitchType());
        venue.setStraightBoundaryMeters(venueDTO.getStraightBoundaryMeters());
        venue.setSquareBoundaryMeters(venueDTO.getSquareBoundaryMeters());
        venue.setCapacity(venueDTO.getCapacity());
        venue.setTimeZone(venueDTO.getTimeZone());
        return modelMapper.map(venueRepository.save(venue), VenueDTO.class);
    }
}
