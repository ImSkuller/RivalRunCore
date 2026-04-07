package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.TeamsManager;

public class LeaveEvent implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        final Player player = event.getPlayer();
        TeamsManager teamManager = RivalRun.getInstance().getTeamManager();

        Teams team = teamManager.getPlayerTeam(player);

        Component quitMessage;

        if (team != null) {

            quitMessage = Component.text("")
                    .append(Component.text(player.getName(), team.getColor()))
                    .append(Component.text(" left the server", NamedTextColor.RED));

        } else {

            quitMessage = Component.text("")
                    .append(Component.text(player.getName(), NamedTextColor.WHITE))
                    .append(Component.text(" left the server", NamedTextColor.RED));
        }

        event.quitMessage(quitMessage);
    }
}