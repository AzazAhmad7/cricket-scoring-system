package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.BatterStatsDTO;
import com.cricket.scoring.dtos.BowlerStatsDTO;
import com.cricket.scoring.entities.BatterStats;
import com.cricket.scoring.entities.BowlerStats;
import com.cricket.scoring.repositories.BowlerStatsRepository;
import com.cricket.scoring.services.BowlerStatsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BowlerStatsServiceImpl implements BowlerStatsService {

    private final BowlerStatsRepository bowlerStatsRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<BowlerStatsDTO> getAllBowlerStatsByPlayerId(Long playerId) {
        List<BowlerStats> bowlerStatsList = bowlerStatsRepository.findByPlayerId(playerId);
        return bowlerStatsList.stream()
                .map((element) -> modelMapper.map(element, BowlerStatsDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<BowlerStatsDTO> getAllBowlerStatsByTournamentId(Long tournamentId) {
        List<BowlerStats> bowlerStatsList = bowlerStatsRepository.findByTournamentId(tournamentId);
        return bowlerStatsList.stream()
                .map((element) -> modelMapper.map(element, BowlerStatsDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<BowlerStatsDTO> getAllBowlerStats() {
        List<BowlerStats> bowlerStatsList = bowlerStatsRepository.findAll();
        return bowlerStatsList.stream()
                .map((element) -> modelMapper.map(element, BowlerStatsDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public BowlerStatsDTO getBowlerStatsOfPlayer(Long playerId, Long tournamentId) {
        BowlerStats batterStats = bowlerStatsRepository.findByTournamentIdAndPlayerId(tournamentId, playerId);
        return modelMapper.map(batterStats, BowlerStatsDTO.class);
    }

    @Override
    public Boolean statsExistByPlayerId(Long playerId) {
        return bowlerStatsRepository.existsByPlayerId(playerId);
    }

    @Override
    public BowlerStatsDTO addBowlerStats(BowlerStatsDTO bowlerStatsDTO) {
        BowlerStats bs = modelMapper.map(bowlerStatsDTO, BowlerStats.class);
        return modelMapper.map(bowlerStatsRepository.save(bs), BowlerStatsDTO.class);
    }
}
