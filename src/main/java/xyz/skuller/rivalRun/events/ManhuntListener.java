package xyz.skuller.rivalRun.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.helpers.TeamRole;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.ManhuntManager;
import xyz.skuller.rivalRun.managers.SpectatorManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

// Manhunt's death/respawn rules. Hunters respawn indefinitely (so there's
// nothing to do for them beyond keeping their tracking compass); a
// Speedrunner's death instead converts them to a (team-restricted)
// spectator on respawn, and if that empties their whole team, the team
// gets folded into the Hunters.
public class ManhuntListener implements Listener {

    private boolean active() {
        RivalRun plugin = RivalRun.getInstance();
        return plugin.getGamemodeManager().isManhunt()
                && plugin.getGameStateManager().isState(GameStateManager.GameStates.RUNNING);
    }

    // Hunters keep their tracking compass through death instead of it
    // dropping on the ground - it gets re-given on respawn regardless.
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!active()) return;

        RivalRun plugin = RivalRun.getInstance();
        TeamRole role = plugin.getTeamManager().getTeamRole(event.getEntity());
        if (role != TeamRole.HUNTER) return;

        ManhuntManager manhuntManager = plugin.getManhuntManager();
        event.getDrops().removeIf(manhuntManager::isTrackerCompass);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!active()) return;

        Player player = event.getPlayer();
        RivalRun plugin = RivalRun.getInstance();
        TeamsManager teamsManager = plugin.getTeamManager();
        TeamRole role = teamsManager.getTeamRole(player);

        if (role == TeamRole.HUNTER) {
            plugin.getManhuntManager().giveTrackerCompass(player);
            return;
        }

        if (role != TeamRole.SPEEDRUNNER) return;

        Teams team = teamsManager.getPlayerTeam(player);
        SpectatorManager spectatorManager = plugin.getSpectatorManager();

        spectatorManager.makeSpectator(player);

        if (team != null && spectatorManager.countAlive(team) == 0) {
            plugin.getManhuntManager().convertTeamToHunters(team);
        }
    }

}
