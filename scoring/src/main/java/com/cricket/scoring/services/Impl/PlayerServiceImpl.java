package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.PlayerDTO;
import com.cricket.scoring.dtos.TeamDetailsDTO;
import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.repositories.PlayerRepository;
import com.cricket.scoring.services.PlayerService;
import com.cricket.scoring.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public PlayerDTO getPlayerById(Long id) {
        Player player = playerRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Player not found with id: " + id));
        PlayerDTO playerDTO = modelMapper.map(player, PlayerDTO.class);
        TeamDetailsDTO dto = null;
        if(player.getTeam() != null) {
             dto = TeamDetailsDTO.builder()
                    .id(player.getId())
                    .name(player.getTeam().getName())
                    .shortName(player.getTeam().getShortName())
                    .build();
        }
        playerDTO.setTeamDTO(dto);
        return playerDTO;
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
        Player player = playerRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Player not found with id: " + id));
        playerRepository.delete(player);
    }

    @Override
    @Transactional
    public Player assignPlayerToTeam(Long playerId, Long teamId) {
        Player player = playerRepository.findById(playerId).orElseThrow(()->new ResourceNotFoundException("Player not found with id: " + playerId));
        Team team = modelMapper.map(teamService.getTeam(teamId), Team.class);
        player.setTeam(team);
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public List<Player> assignPlayersToTeams(List<Long> playerIds, Long teamId) {
        Team team = modelMapper.map(teamService.getTeam(teamId), Team.class);
        List<Player> players = new ArrayList<>();
        for(Long playerId : playerIds) {
            Player player = playerRepository.findById(playerId).orElseThrow(()->new ResourceNotFoundException("Player not found with id: " + playerId));
            player.setTeam(team);
            players.add(player);
        }
        return playerRepository.saveAll(players);
    }

    @Override
    @Transactional
    public Player removePlayerFromTeam(Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(()->new ResourceNotFoundException("Player not found with id: " + playerId));
        player.setTeam(null);
        return playerRepository.save(player);
    }

    @Override
    public Player updatePlayer(Long playerId, Player playerRequest) {
        Player player = playerRepository.findById(playerId).orElseThrow(()->new ResourceNotFoundException("Player not found with id: " + playerId));
        if(playerRequest.getExternalPlayerId() != null) player.setExternalPlayerId(playerRequest.getExternalPlayerId());
        if(playerRequest.getFullName()!= null) player.setFullName(playerRequest.getFullName());
        if(playerRequest.getShortName() != null) player.setShortName(playerRequest.getShortName());
        if(playerRequest.getJerseyNumber() != null) player.setJerseyNumber(playerRequest.getJerseyNumber());
        if(playerRequest.getDateOfBirth() != null) player.setDateOfBirth(playerRequest.getDateOfBirth());
        if(playerRequest.getNationality() != null) player.setNationality(playerRequest.getNationality());
        if(playerRequest.getRole() != null) player.setRole(playerRequest.getRole());
        if(playerRequest.getBattingStyle() != null) player.setBattingStyle(playerRequest.getBattingStyle());
        if(playerRequest.getBowlingStyle() != null) player.setBowlingStyle(playerRequest.getBowlingStyle());
        if(playerRequest.getCaptain() != null) player.setCaptain(playerRequest.getCaptain());
        if(playerRequest.getWicketKeeper() != null) player.setWicketKeeper(playerRequest.getWicketKeeper());
        if(playerRequest.getBattingOrder() != null) player.setBattingOrder(playerRequest.getBattingOrder());
        if(playerRequest.getActive() != null) player.setActive(playerRequest.getActive());
        if(playerRequest.getMatchesPlayed() != null) player.setMatchesPlayed(playerRequest.getMatchesPlayed());
        if(playerRequest.getRuns() != null) player.setRuns(playerRequest.getRuns());
        if(playerRequest.getWickets() != null) player.setWickets(playerRequest.getWickets());
        if(playerRequest.getBattingAverage() != null) player.setBattingAverage(playerRequest.getBattingAverage());
        if(playerRequest.getBowlingAverage() != null) player.setBowlingAverage(playerRequest.getBowlingAverage());
        return playerRepository.save(player);
    }
}
