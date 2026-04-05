package xyz.skuller.rivalRun.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.skuller.rivalRun.helpers.Teams;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamsManager {

    private final Map<String, Teams> teams = new HashMap<>();
    private final Map<UUID, Teams> playerTeams = new HashMap<>();

    public void createTeam(String name, ChatColor color) {
        teams.put(name, new Teams(name, color));
    }

    public void assignPlayer(Player player, Teams team) {
        playerTeams.put(player.getUniqueId(), team);
        team.addPlayer(player.getUniqueId());
    }

    public Teams getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    public Collection<Teams> getTeams() {
        return teams.values();
    }

}
