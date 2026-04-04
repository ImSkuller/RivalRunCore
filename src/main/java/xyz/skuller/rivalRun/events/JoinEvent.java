package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        final Player player = event.getPlayer();


        // TODO: Make all these values actually real values.
        int teamID = 1;
        NamedTextColor teamColor = NamedTextColor.YELLOW;


        if (teamID == 1) {

            // Join message for people who belong to a team.
            Component joinMessage = Component.text("")
                    .append(player.displayName().color(teamColor))
                    .append(Component.text(" Joined The Server", NamedTextColor.GREEN));

            event.joinMessage(joinMessage);
        } else {

            // Join message for people who are not in a team.
            Component joinMessage = Component.text("")
                    .append(player.displayName().color(NamedTextColor.WHITE))
                    .append(Component.text(" Joined The Server", NamedTextColor.GREEN));

            event.joinMessage(joinMessage);
        }
    }
}
