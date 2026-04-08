package xyz.skuller.rivalRun.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.TeamsManager;

public class TeamChatFormatter implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        Player player = event.getPlayer();

        event.renderer((source, sourceDisplayName, message, viewer) -> {
           if (!(viewer instanceof Player)) {
               return message;
           }

            TeamsManager teamsManager = RivalRun.getInstance().getTeamManager();
            Teams team = teamsManager.getPlayerTeam(player);
            Component prefix;

            if (team == null) {
                prefix = Component.text("[None] ")
                        .color(NamedTextColor.GOLD);
            } else {
                prefix = Component.text("[" + team.getName() + "] ")
                        .color(team.getColor());
            }

            return prefix
                    .append(sourceDisplayName)
                    .append(Component.text(" » ").color(NamedTextColor.GRAY))
                    .append((message).color(NamedTextColor.WHITE))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to /msg " + source.getName(), NamedTextColor.YELLOW)))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + source.getName()));
        });
    }

}
