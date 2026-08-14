package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.TeamsManager;
import xyz.skuller.rivalRun.menus.TeamSelectMenu;

public class JoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        final Player player = event.getPlayer();
        RivalRun plugin = RivalRun.getInstance();
        TeamsManager teamManager = plugin.getTeamManager();

        // First-time joiners (and anyone whose last location was in a world
        // deleted by a previous /rivalrun resetworld) land in the server's
        // default world by vanilla behavior - RivalRun never plays there.
        World currentOverworld = plugin.getWorldResetManager().getCurrentOverworld();
        if (currentOverworld != null && !player.getWorld().equals(currentOverworld)) {
            player.teleport(currentOverworld.getSpawnLocation());
        }

        Teams team = teamManager.getPlayerTeam(player);

        Component joinMessage;

        if (team != null) {

            joinMessage = Component.text("")
                    .append(Component.text(player.getName(), team.getColor()))
                    .append(Component.text(" joined the server", NamedTextColor.GREEN));

        } else {

            GameStateManager gsm = plugin.getGameStateManager();
            joinMessage = Component.text("")
                    .append(Component.text(player.getName(), NamedTextColor.WHITE))
                    .append(Component.text(" joined the server", NamedTextColor.GREEN));


            if (gsm.isState(GameStateManager.GameStates.WAITING)) {
                if (plugin.getConfig().getBoolean("teams.loginGUI", true)) {
                    new TeamSelectMenu().open(player);
                }
            }
            else if (gsm.isState(GameStateManager.GameStates.POST)) {
                player.sendRichMessage("<red>You joined a tad late, a game just ended.");
            }
            else {
                plugin.getSpectatorManager().makeSpectator(player);
                player.sendRichMessage("<red>A game is going on right now please wait till it ends before you can play again.");
            }

        }

        event.joinMessage(joinMessage);

        // Snappy first-second experience instead of waiting for the 1s tick
        plugin.getScoreboardManager().refresh(player);
        plugin.getTabListManager().refresh(player);
    }

    // Without a bed/anchor, Bukkit's default respawn location can end up
    // pointing at whichever world the player died in - if that was the
    // dedicated world set /rivalrun resetworld just removed (or a player
    // never redirected out of the server's real default world), always
    // fall back to the current world's spawn instead of wherever vanilla
    // would otherwise pick. This is what makes a fresh /rivalrun resetworld
    // the actual respawn point going forward, not the old/deleted world.
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (event.isBedSpawn() || event.isAnchorSpawn()) return;

        World currentOverworld = RivalRun.getInstance().getWorldResetManager().getCurrentOverworld();
        if (currentOverworld == null) return;

        event.setRespawnLocation(currentOverworld.getSpawnLocation());
    }
}
