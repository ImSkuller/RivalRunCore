package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEvent implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        final Player player = event.getPlayer();


        // TODO: Make all these values actually real instead of placeholders.

        int teamID = 1;
        NamedTextColor teamColor = NamedTextColor.YELLOW;

        if (teamID == 1) {

            // Quit message for people who belong to a team.
            Component quitMessage = Component.text("")
                    .append(player.displayName().color(teamColor))
                    .append(Component.text(" Left The Server", NamedTextColor.RED));

            event.quitMessage(quitMessage);
        }
        else {

            // Quit for people who are not in a team.
            Component quitMessage = Component.text("")
                    .append(player.displayName().color(NamedTextColor.WHITE))
                    .append(Component.text(" Left The Server", NamedTextColor.RED));

            event.quitMessage(quitMessage);
        }

    }
}
