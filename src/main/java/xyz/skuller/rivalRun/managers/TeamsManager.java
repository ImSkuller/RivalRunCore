package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.TeamPresets;
import xyz.skuller.rivalRun.helpers.Teams;

import java.util.*;

public class TeamsManager {

    private boolean teamsLocked = false;
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
    public boolean assignPlayer(Player player, Teams newTeam) {

        if (teamsLocked) return false;
        if (newTeam == null) return false;

        int max = plugin.getConfig().getInt("teams.max");

        if (newTeam.getSize() >= max) {
            player.sendMessage("§cThat team is full!");
            return false;
        }

        UUID uuid = player.getUniqueId();

        if (playerTeams.containsKey(uuid)) {
            Teams oldTeam = playerTeams.get(uuid);
            oldTeam.removePlayer(uuid);
        }

        playerTeams.put(uuid, newTeam);
        newTeam.addPlayer(uuid);
        applyNametag(player);
        updateTab(player);

        return true;
    }

    // Get player's current team function
    public Teams getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    // Get all the players in the team.
    public List<String> getPlayerNames(Teams team) {
        List<String> names = new ArrayList<>();

        for (UUID uuid : team.getPlayers()) {
            String name = plugin.getServer().getOfflinePlayer(uuid).getName();

            if (name != null) {
                names.add(name);
            }
        }

        return names;
    }

    // Applies the team color to the player
    public void applyNametag(Player player) {

        Teams team = getPlayerTeam(player);
        if (team == null) return;

        var scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();

        Team sbTeam = scoreboard.getTeam(team.getName());

        if (sbTeam == null) {
            sbTeam = scoreboard.registerNewTeam(team.getName());
        }

        sbTeam.color(team.getColor());
        sbTeam.addEntry(player.getName());
    }

    // Updates the player's nametag in tab list
    public void updateTab(Player player) {
        Teams team = getPlayerTeam(player);
        if (team == null) return;

        String prefix = "[" + team.getName() + "] ";

        player.setPlayerListName(prefix + player.getName());
    }

    // Function to help get the teams by its name
    public Teams getTeamByName(String name) {
        return teams.get(name);
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

    // Get the names and colors of the teams (formatted)
    public List<String> getFormattedTeams() {
        List<String> list = new ArrayList<>();

        for (Teams team : teams.values()) {
            list.add("\n" + team.getName() + " (" +team.getColor() + ")" +  " Players " + team.getSize() + "\n");
        }

        return list;
    }

    // Returns if the teams are full or not
    public boolean isTeamFull(Teams team) {
        int max = plugin.getConfig().getInt("teams.max");
        return team.getSize() >= max;
    }


    // Functions used to lock and unlock the teams
    public void lockTeams() {
        teamsLocked = true;
    }

    public void unlockTeams() {
        teamsLocked = false;
    }

    // Function to reset all the teams and players
    public void reset() {
        teams.clear();
        playerTeams.clear();
        teamsLocked = false;
    }
}