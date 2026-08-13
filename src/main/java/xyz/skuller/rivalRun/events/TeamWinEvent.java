package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.GameStateManager;

public class TeamWinEvent implements Listener {

    private final Plugin plugin;

    public TeamWinEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof EnderDragon)) return;
        if (!RivalRun.getInstance().getGameStateManager().isState(GameStateManager.GameStates.RUNNING)) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Teams team = RivalRun.getInstance().getTeamManager().getPlayerTeam(killer);
        if (team == null) return;

        Bukkit.broadcast(Component.text("Team " + team.getName() + " has won the game!", NamedTextColor.GOLD));
        RivalRun.getInstance().getGameStateManager().setState(GameStateManager.GameStates.POST);
    }
}
