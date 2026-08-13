package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Tracks which players are currently spectating (as opposed to actively
// playing a run) and drives the elimination win condition once only one
// team has any non-spectating members left.
public class SpectatorManager {

    private final Set<UUID> spectating = new HashSet<>();

    public boolean isSpectating(Player player) {
        return spectating.contains(player.getUniqueId());
    }

    public int countAlive(Teams team) {
        int alive = 0;
        for (UUID uuid : team.getPlayers()) {
            if (!spectating.contains(uuid)) alive++;
        }
        return alive;
    }

    // Converts a player to a spectator - either because they joined with no
    // team while a game is already running, or an admin sent them in.
    public void makeSpectator(Player player) {
        if (!spectating.add(player.getUniqueId())) return;

        player.setGameMode(GameMode.SPECTATOR);

        RivalRun plugin = RivalRun.getInstance();
        if (plugin.getConfig().getBoolean("spectator.teleportMenu", true)) {
            giveSpectateCompass(player);
        }

        checkEliminationWin();
    }

    // Returns a spectating player back to survival and clears their tracking.
    public void clearSpectator(Player player) {
        if (!spectating.remove(player.getUniqueId())) return;

        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setItem(0, null);
    }

    // Flips a player's spectating state. Returns true if they are now
    // spectating, false if they were just restored to survival.
    public boolean toggleSpectator(Player player) {
        if (isSpectating(player)) {
            clearSpectator(player);
            return false;
        }

        makeSpectator(player);
        return true;
    }

    public void reset() {
        spectating.clear();
    }

    private void giveSpectateCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.displayName(Component.text("Spectate a Player", NamedTextColor.AQUA));
        compass.setItemMeta(meta);
        player.getInventory().setItem(0, compass);
    }

    // If exactly one team still has non-spectating players left (and there
    // was more than one team in play to begin with), that team auto-wins.
    private void checkEliminationWin() {
        RivalRun plugin = RivalRun.getInstance();
        if (!plugin.getConfig().getBoolean("spectator.eliminationWin", true)) return;
        if (!plugin.getGameStateManager().isState(GameStateManager.GameStates.RUNNING)) return;

        TeamsManager tm = plugin.getTeamManager();

        long populatedTeams = tm.getTeams().stream().filter(t -> t.getSize() > 0).count();
        if (populatedTeams < 2) return;

        Teams onlyRemaining = null;
        int teamsAlive = 0;

        for (Teams team : tm.getTeams()) {
            if (team.getSize() == 0) continue;
            if (countAlive(team) > 0) {
                teamsAlive++;
                onlyRemaining = team;
            }
        }

        if (teamsAlive == 1) {
            plugin.getWinManager().announceWin(onlyRemaining);
        }
    }

}
