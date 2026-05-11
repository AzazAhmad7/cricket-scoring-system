package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.PlayerDTO;
import com.cricket.scoring.dtos.TeamDTO;
import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.repositories.PlayerRepository;
import com.cricket.scoring.repositories.TeamRepository;
import com.cricket.scoring.services.PlayerService;
import com.cricket.scoring.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    private final TeamService teamService;
    private final ModelMapper modelMapper;

    @Override
    public Player createPlayer(Player Player) {
        return playerRepository.save(Player);
    }

    @Override
    public void createPlayers(List<Player> players) {
        for(Player player : players) {
            createPlayer(player);
        }
    }

    @Override
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Player not found with id: " + id));
    }

    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public List<Player> getPlayersByTeam(Long id){
        Team team = modelMapper.map(teamService.getTeam(id), Team.class);
        return playerRepository.findByTeam(team);
    }

    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    public Player assignPlayerToTeam(Long playerId, Long teamId) {
        Player player = getPlayerById(playerId);
        Team team = modelMapper.map(teamService.getTeam(teamId), Team.class);
        player.setTeam(team);
        return playerRepository.save(player);
    }

    @Override
    public Player removePlayerFromTeam(Long playerId) {
        Player player = getPlayerById(playerId);
        player.setTeam(null);
        return playerRepository.save(player);
    }

    @Override
    public Player updatePlayer(Long playerId, Player Player) {
        Player player = playerRepository.findById(playerId).orElseThrow(()->new ResourceNotFoundException("Player not found with id: " + playerId));
        if(player.getExternalPlayerId() != null) player.setExternalPlayerId(player.getExternalPlayerId());
        if(player.getFullName()!= null) player.setFullName(player.getFullName());
        if(player.getShortName() != null) player.setShortName(player.getShortName());
        if(player.getJerseyNumber() != null) player.setJerseyNumber(player.getJerseyNumber());
        if(player.getDateOfBirth() != null) player.setDateOfBirth(player.getDateOfBirth());
        if(player.getNationality() != null) player.setNationality(player.getNationality());
        if(player.getRole() != null) player.setRole(player.getRole());
        if(player.getBattingStyle() != null) player.setBattingStyle(player.getBattingStyle());
        if(player.getBowlingStyle() != null) player.setBowlingStyle(player.getBowlingStyle());
        if(player.getCaptain() != null) player.setCaptain(player.getCaptain());
        if(player.getWicketKeeper() != null) player.setWicketKeeper(player.getWicketKeeper());
        if(player.getBattingOrder() != null) player.setBattingOrder(player.getBattingOrder());
        if(player.getActive() != null) player.setActive(player.getActive());
        if(player.getMatchesPlayed() != null) player.setMatchesPlayed(player.getMatchesPlayed());
        if(player.getRuns() != null) player.setRuns(player.getRuns());
        if(player.getWickets() != null) player.setWickets(player.getWickets());
        if(player.getBattingAverage() != null) player.setBattingAverage(player.getBattingAverage());
        if(player.getBowlingAverage() != null) player.setBowlingAverage(player.getBowlingAverage());
        return playerRepository.save(player);
    }
}
