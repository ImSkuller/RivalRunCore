package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Messages;
import xyz.skuller.rivalRun.helpers.TeamPresets;
import xyz.skuller.rivalRun.helpers.Teams;

import java.util.*;

public class TeamsManager {

    private static final String CUSTOM_TEAMS_PATH = "teams.custom";
    private static final int MAX_TEAMS = 12;

    private boolean teamsLocked = false;
    private final RivalRun plugin;

    private final Map<String, Teams> teams = new HashMap<>();
    private final Map<UUID, Teams> playerTeams = new HashMap<>();

    public TeamsManager(RivalRun plugin) {
        this.plugin = plugin;
    }

    // Team Creation - uses teams.custom if any custom teams have ever been
    // saved, otherwise falls back to generating teams from the preset config.
    public void loadTeamsFromConfig() {

        teams.clear();

        List<Map<?, ?>> customTeams = plugin.getConfig().getMapList(CUSTOM_TEAMS_PATH);

        if (!customTeams.isEmpty()) {
            for (Map<?, ?> entry : customTeams) {
                String name = String.valueOf(entry.get("name"));
                NamedTextColor color = NamedTextColor.NAMES.value(String.valueOf(entry.get("color")));
                createTeam(name, color != null ? color : NamedTextColor.WHITE);
            }

            Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Loaded <gold>" + teams.size() + "<green> custom teams.");
            return;
        }

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

        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Team count: <gold>" + teamCount);
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Teams loaded: <gold>" + teams.size());
    }

    // Create Team function
    public void createTeam(String name, NamedTextColor color) {
        teams.put(name, new Teams(name, color));
    }

    // Adds a brand new team and persists the current full team roster to
    // teams.custom, so it (and every other currently active team) survives
    // a restart. Fails if a team with that name already exists.
    public boolean addCustomTeam(String name, NamedTextColor color) {
        if (teams.containsKey(name)) return false;
        if (teams.size() >= MAX_TEAMS) return false;

        createTeam(name, color);
        persistCustomTeams();
        return true;
    }

    // Removes a team, refusing if it still has players on it. Persists the
    // remaining roster the same way addCustomTeam does.
    public boolean removeCustomTeam(Teams team) {
        if (team.getSize() > 0) return false;

        teams.remove(team.getName());
        persistCustomTeams();
        return true;
    }

    private void persistCustomTeams() {
        List<Map<String, Object>> serialized = new ArrayList<>();

        for (Teams team : teams.values()) {
            String colorKey = NamedTextColor.NAMES.key(team.getColor());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", team.getName());
            entry.put("color", colorKey != null ? colorKey : "white");
            serialized.add(entry);
        }

        plugin.getConfig().set(CUSTOM_TEAMS_PATH, serialized);
        plugin.saveConfig();
    }

    // Assign Player Function
    public boolean assignPlayer(Player player, Teams newTeam) {

        if (teamsLocked) {
            player.sendMessage(Messages.get("messages.teamsLocked"));
            return false;
        }
        if (newTeam == null) return false;

        int max = plugin.getConfig().getInt("teams.max");

        if (newTeam.getSize() >= max) {
            player.sendMessage(Messages.get("messages.teamFull"));
            return false;
        }

        UUID uuid = player.getUniqueId();

        if (playerTeams.containsKey(uuid)) {
            Teams oldTeam = playerTeams.get(uuid);
            oldTeam.removePlayer(uuid);
        }

        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Added <red>" + player + "<green> to team <red>" + newTeam);

        playerTeams.put(uuid, newTeam);
        newTeam.addPlayer(uuid);
        RivalRun.getInstance().getScoreboardManager().refresh(player);

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

    // Returns the max team size
    public int getMaxTeamSize() {
        return plugin.getConfig().getInt("teams.max");
    }

    // Checks if the player is already in a team or not
    public boolean isInTeam(Player player) {
        return playerTeams.containsKey(player.getUniqueId());
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
            RivalRun.getInstance().getScoreboardManager().refresh(player);
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
        RivalRun.getInstance().getSpectatorManager().reset();
        loadTeamsFromConfig();
    }
}