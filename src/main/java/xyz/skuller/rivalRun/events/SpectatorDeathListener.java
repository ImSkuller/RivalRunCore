package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Messages;
import xyz.skuller.rivalRun.managers.GameStateManager;

// Turns dying mid-run into becoming a spectator instead of a normal respawn,
// when spectator.deathBecomesSpectator is enabled.
public class SpectatorDeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();

        Bukkit.broadcast(Messages.get("messages.eliminated", "player", player.getName())
                .colorIfAbsent(NamedTextColor.RED));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        RivalRun plugin = RivalRun.getInstance();

        if (plugin.getTeamManager().getPlayerTeam(player) == null) return;
        if (plugin.getSpectatorManager().isSpectating(player)) return;

        Bukkit.getScheduler().runTask(plugin, () -> plugin.getSpectatorManager().makeSpectator(player));
    }

    private boolean isEnabled() {
        RivalRun plugin = RivalRun.getInstance();
        return plugin.getConfig().getBoolean("spectator.deathBecomesSpectator", true)
                && plugin.getGameStateManager().isState(GameStateManager.GameStates.RUNNING);
    }

}
