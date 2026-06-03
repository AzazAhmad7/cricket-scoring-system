package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.BatterStatsDTO;
import com.cricket.scoring.entities.BatterStats;
import com.cricket.scoring.repositories.BatterStatsRepository;
import com.cricket.scoring.services.BatterStatsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatterStatsServiceImpl implements BatterStatsService {

    private final BatterStatsRepository batterStatsRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<BatterStatsDTO> getAllBatterStatsByPlayerId(Long playerId) {
        List<BatterStats> batterStatsList = batterStatsRepository.findByPlayerId(playerId);
        return batterStatsList.stream()
                .map((element) -> modelMapper.map(element, BatterStatsDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<BatterStatsDTO> getAllBatterStatsByTournamentId(Long tournamentId) {
        List<BatterStats> batterStatsList = batterStatsRepository.findByTournamentId(tournamentId);
        return batterStatsList.stream()
                .map((element) -> modelMapper.map(element, BatterStatsDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<BatterStatsDTO> getAllBatterStats() {
        List<BatterStats> batterStatsList = batterStatsRepository.findAll();
        return batterStatsList.stream()
                .map((element) -> modelMapper.map(element, BatterStatsDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public BatterStatsDTO getBatterStatsOfPlayer(Long playerId, Long tournamentId) {
        BatterStats batterStats = batterStatsRepository.findByTournamentIdAndPlayerId(tournamentId, playerId);
        return modelMapper.map(batterStats, BatterStatsDTO.class);
    }

    @Override
    public Boolean statsExistByPlayerId(Long playerId) {
        return batterStatsRepository.existsByPlayerId(playerId);
    }

    @Override
    public BatterStatsDTO addBatterStats(BatterStatsDTO batterStatsDTO) {
        BatterStats bs = modelMapper.map(batterStatsDTO, BatterStats.class);
        return modelMapper.map(batterStatsRepository.save(bs), BatterStatsDTO.class);
    }
}
