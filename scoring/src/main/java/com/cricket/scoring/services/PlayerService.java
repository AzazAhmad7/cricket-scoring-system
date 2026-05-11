package com.cricket.scoring.services;


import com.cricket.scoring.entities.Player;
import com.cricket.scoring.entities.Team;

import java.util.List;

public interface PlayerService {
    public Player createPlayer(Player Player);
    public void createPlayers(List<Player> players);
    public Player getPlayerById(Long id);
    public List<Player> getAllPlayers();
    public List<Player> getPlayersByTeam(Long id);
    public void deletePlayer(Long id);
    public Player assignPlayerToTeam(Long playerId, Long teamId);
    public Player removePlayerFromTeam(Long playerId);
    public Player updatePlayer(Long playerId, Player Player);
}
