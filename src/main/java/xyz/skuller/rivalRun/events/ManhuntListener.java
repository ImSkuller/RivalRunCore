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
    //
    // For a Speedrunner, decides here (while the killer is still known)
    // whether this death is permanent per manhunt.permaDeathMode - either
    // every death counts, or only ones where a Hunter landed the kill. The
    // decision is stashed in ManhuntManager and consumed in onRespawn(),
    // since PlayerRespawnEvent has no death-cause info of its own.
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!active()) return;

        RivalRun plugin = RivalRun.getInstance();
        Player victim = event.getEntity();
        TeamsManager teamsManager = plugin.getTeamManager();
        TeamRole role = teamsManager.getTeamRole(victim);
        ManhuntManager manhuntManager = plugin.getManhuntManager();

        if (role == TeamRole.HUNTER) {
            event.getDrops().removeIf(manhuntManager::isTrackerCompass);
            return;
        }

        if (role != TeamRole.SPEEDRUNNER) return;

        Player killer = victim.getKiller();
        TeamRole killerRole = killer != null ? teamsManager.getTeamRole(killer) : TeamRole.NONE;

        if (manhuntManager.isPermanentDeath(killerRole)) {
            manhuntManager.markPermanentDeath(victim);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!active()) return;

        Player player = event.getPlayer();
        RivalRun plugin = RivalRun.getInstance();
        TeamsManager teamsManager = plugin.getTeamManager();
        TeamRole role = teamsManager.getTeamRole(player);
        ManhuntManager manhuntManager = plugin.getManhuntManager();

        if (role == TeamRole.HUNTER) {
            manhuntManager.giveTrackerCompass(player);
            return;
        }

        if (role != TeamRole.SPEEDRUNNER) return;

        // Not marked permanent (e.g. Hunter-Kills-Only mode and this was a
        // fall/mob/lava death) - just a normal respawn, keep playing.
        if (!manhuntManager.consumePermanentDeath(player)) return;

        Teams team = teamsManager.getPlayerTeam(player);
        SpectatorManager spectatorManager = plugin.getSpectatorManager();

        spectatorManager.makeSpectator(player);

        if (team != null && spectatorManager.countAlive(team) == 0) {
            manhuntManager.convertTeamToHunters(team);
        }
    }

}
