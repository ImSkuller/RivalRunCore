package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.TeamsManager;
import xyz.skuller.rivalRun.menus.TeamSelectMenu;

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
            
            GameStateManager gsm = RivalRun.getInstance().getGameStateManager();
            joinMessage = Component.text("")
                    .append(Component.text(player.getName(), NamedTextColor.WHITE))
                    .append(Component.text(" joined the server", NamedTextColor.GREEN));


            if (gsm.isState(GameStateManager.GameStates.WAITING)) {
                new TeamSelectMenu().open(player);
            }
            else if (gsm.isState(GameStateManager.GameStates.POST)) {
                player.sendRichMessage("<red>You joined a tad late, a game just ended.");
            }
            else {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendRichMessage("<red>A game is going on right now please wait till it ends before you can play again.");
            }

        }

        event.joinMessage(joinMessage);
    }
}
