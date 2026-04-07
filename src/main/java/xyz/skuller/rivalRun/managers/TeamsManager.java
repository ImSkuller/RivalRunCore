package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.TeamPresets;
import xyz.skuller.rivalRun.helpers.Teams;

import java.util.*;

public class TeamsManager {

    private final RivalRun plugin;

    private final Map<String, Teams> teams = new HashMap<>();
    private final Map<UUID, Teams> playerTeams = new HashMap<>();

    public TeamsManager(RivalRun plugin) {
        this.plugin = plugin;
    }

    // Team Creation (Automatic from the config)
    public void loadTeamsFromConfig() {

        teams.clear();

        int teamCount = plugin.getConfig().getInt("teams.list");
        int presetId = plugin.getConfig().getInt("teams.teamName");

        // Validate team count
        if (teamCount < 2 || teamCount > 7) {
            throw new IllegalArgumentException("Teams must be between 2 and 7");
        }

        TeamPresets.TeamPreset[] presets = TeamPresets.TeamPreset.values();

        // Safe preset selection
        if (presetId < 0 || presetId >= presets.length) {
            presetId = 0;
        }

        TeamPresets.TeamPreset preset = presets[presetId];
        String[] names = preset.getNames();

        for (int i = 0; i < teamCount; i++) {

            String teamName = names[i];
            NamedTextColor color = TeamPresets.getColorByIndex(i);

            createTeam(teamName, color);
        }
    }

    // Create Team function
    public void createTeam(String name, NamedTextColor color) {
        teams.put(name, new Teams(name, color));
    }

    // Assign Player Function
    public void assignPlayer(Player player, Teams newTeam) {
        UUID uuid = player.getUniqueId();

        // Remove from old team
        if (playerTeams.containsKey(uuid)) {
            Teams oldTeam = playerTeams.get(uuid);
            oldTeam.removePlayer(uuid);
        }

        // Add to new team
        playerTeams.put(uuid, newTeam);
        newTeam.addPlayer(uuid);
    }

    // Get player's current team function
    public Teams getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    // Function used to get all the teams that are currently in the game
    public Collection<Teams> getTeams() {
        return teams.values();
    }

    // Function used to remove player from a team
    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();

        Teams team = playerTeams.remove(uuid);
        if (team != null) {
            team.removePlayer(uuid);
        }
    }

    // Function to reset all the teams and players
    public void reset() {
        teams.clear();
        playerTeams.clear();
    }
}