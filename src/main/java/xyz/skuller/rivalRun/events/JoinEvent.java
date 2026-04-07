package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.TeamsManager;

public class JoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        final Player player = event.getPlayer();
        TeamsManager teamManager = RivalRun.getInstance().getTeamManager();

        Teams team = teamManager.getPlayerTeam(player);

        // Apply visuals if player has a team
        if (team != null) {
            teamManager.applyNametag(player);
            teamManager.updateTab(player);
        }

        Component joinMessage;

        if (team != null) {

            joinMessage = Component.text("")
                    .append(Component.text(player.getName(), team.getColor()))
                    .append(Component.text(" joined the server", NamedTextColor.GREEN));

        } else {

            joinMessage = Component.text("")
                    .append(Component.text(player.getName(), NamedTextColor.WHITE))
                    .append(Component.text(" joined the server", NamedTextColor.GREEN));
        }

        event.joinMessage(joinMessage);
    }
}
