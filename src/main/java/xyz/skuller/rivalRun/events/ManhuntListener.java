package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.helpers.TeamRole;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.ManhuntManager;
import xyz.skuller.rivalRun.managers.SpectatorManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

import java.util.UUID;

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
        } else {
            // Team isn't wiped - lock this dead Speedrunner to spectating
            // through whichever teammate is still alive (see the
            // move/interact restrictions below) instead of free-flying.
            manhuntManager.bindSpectatorToTeammate(player);
        }
    }

    // Tracks damage each Speedrunner team deals to the dragon, so a fair
    // winner can be picked if a Hunter ends up landing the killing blow
    // (see TeamWinEvent) - Speedrunners beating the game is how they win,
    // not whoever happens to get the kill credit.
    @EventHandler
    public void onDragonDamage(EntityDamageByEntityEvent event) {
        if (!active()) return;
        if (!(event.getEntity() instanceof EnderDragon)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        RivalRun plugin = RivalRun.getInstance();
        Teams team = plugin.getTeamManager().getPlayerTeam(player);
        if (team != null && team.getRole() == TeamRole.SPEEDRUNNER) {
            plugin.getManhuntManager().recordDragonDamage(team, event.getFinalDamage());
        }
    }

    // A dead Speedrunner locked to spectating through a teammate can look
    // around (and check F3) freely, but can't fly off on their own - any
    // positional movement is cancelled, only the look direction is kept.
    @EventHandler
    public void onSpectatorMove(PlayerMoveEvent event) {
        RivalRun plugin = RivalRun.getInstance();
        if (!plugin.getManhuntManager().isBoundSpectator(event.getPlayer())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location frozen = from.clone();
            frozen.setYaw(to.getYaw());
            frozen.setPitch(to.getPitch());
            event.setTo(frozen);
        }
    }

    @EventHandler
    public void onSpectatorInteract(PlayerInteractEvent event) {
        if (RivalRun.getInstance().getManhuntManager().isBoundSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpectatorInteractEntity(PlayerInteractEntityEvent event) {
        if (RivalRun.getInstance().getManhuntManager().isBoundSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private static final double HEADSTART_PROTECTION_RADIUS = 10.0;

    // Hunters are helpless (frozen, can't fight back) during the headstart -
    // Speedrunners can't take advantage of that by attacking them or
    // breaking blocks out from under/around them.
    @EventHandler
    public void onHeadstartDamage(EntityDamageByEntityEvent event) {
        RivalRun plugin = RivalRun.getInstance();
        if (!plugin.getGamemodeManager().isManhunt() || !plugin.getGameStateManager().isHeadstart()) return;
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        TeamsManager teamsManager = plugin.getTeamManager();
        if (teamsManager.getTeamRole(damager) != TeamRole.SPEEDRUNNER) return;
        if (teamsManager.getTeamRole(victim) != TeamRole.HUNTER) return;

        event.setCancelled(true);
        damager.sendActionBar(Component.text("You can't attack Hunters during the headstart!", NamedTextColor.RED));
    }

    @EventHandler
    public void onHeadstartBlockBreak(BlockBreakEvent event) {
        RivalRun plugin = RivalRun.getInstance();
        if (!plugin.getGamemodeManager().isManhunt() || !plugin.getGameStateManager().isHeadstart()) return;

        Player player = event.getPlayer();
        if (plugin.getTeamManager().getTeamRole(player) != TeamRole.SPEEDRUNNER) return;

        Teams hunters = plugin.getTeamManager().getHunterTeam();
        if (hunters == null) return;

        for (UUID uuid : hunters.getPlayers()) {
            Player hunter = Bukkit.getPlayer(uuid);
            if (hunter == null || !hunter.getWorld().equals(player.getWorld())) continue;
            if (hunter.getLocation().distance(event.getBlock().getLocation()) <= HEADSTART_PROTECTION_RADIUS) {
                event.setCancelled(true);
                player.sendActionBar(Component.text("You can't break blocks near a Hunter during the headstart!", NamedTextColor.RED));
                return;
            }
        }
    }

}
