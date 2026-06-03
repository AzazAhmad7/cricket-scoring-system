package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.BatterStatsDTO;
import com.cricket.scoring.dtos.FielderTournamentStatsDTO;
import com.cricket.scoring.entities.BatterStats;
import com.cricket.scoring.entities.FielderTournamentStats;
import com.cricket.scoring.repositories.BatterStatsRepository;
import com.cricket.scoring.repositories.FielderTournamentStatsRepository;
import com.cricket.scoring.services.FielderTournametStatsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FielderTournamentStatsServiceImpl implements FielderTournametStatsService {

    private final FielderTournamentStatsRepository fielderTournamentStatsRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<FielderTournamentStatsDTO> getAllFielderStatsByPlayerId(Long playerId) {
        List<FielderTournamentStats> fielderStatsList = fielderTournamentStatsRepository.findByPlayerId(playerId);
        return fielderStatsList.stream()
                .map((element) -> modelMapper.map(element, FielderTournamentStatsDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<FielderTournamentStatsDTO> getAllFielderStatsByTournamentId(Long tournamentId) {
        List<FielderTournamentStats> fielderStatsList = fielderTournamentStatsRepository.findByTournamentId(tournamentId);
        return fielderStatsList.stream()
                .map((element) -> modelMapper.map(element, FielderTournamentStatsDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<FielderTournamentStatsDTO> getAllFielderStats() {
        List<FielderTournamentStats> fielderStatsList = fielderTournamentStatsRepository.findAll();
        return fielderStatsList.stream()
                .map((element) -> modelMapper.map(element, FielderTournamentStatsDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public FielderTournamentStatsDTO getFielderStatsOfPlayer(Long playerId, Long tournamentId) {
        FielderTournamentStats fielderStatsList = fielderTournamentStatsRepository.findByTournamentIdAndPlayerId(tournamentId, playerId);
        return modelMapper.map(fielderStatsList, FielderTournamentStatsDTO.class);
    }
}
