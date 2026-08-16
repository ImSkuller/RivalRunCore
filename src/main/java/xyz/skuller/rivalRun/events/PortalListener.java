package xyz.skuller.rivalRun.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.managers.WorldResetManager;

// RivalRun keeps its Overworld/Nether/End as a dedicated world set (see
// WorldResetManager) that lives alongside whatever other worlds the server
// already has - most notably the server's own default world/world_nether/
// world_the_end trio, which Bukkit always loads regardless of plugins.
// Bukkit's built-in nether/end portal linking has no idea these are meant
// to stay paired together, and left alone will happily send a player who
// walks into a portal in a RivalRun world into that unrelated default
// world instead (and back out into it on the return trip) - exactly the
// "sent to the normal world instead of the RivalRun world" bug. This makes
// portal travel always resolve within the same RivalRun world set the
// player started in, and leaves everything else (e.g. an admin standing in
// the server's default world) to Bukkit's normal behavior.
public class PortalListener implements Listener {

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        World from = event.getFrom().getWorld();
        if (from == null) return;

        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
                && cause != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;

        WorldResetManager worldResetManager = RivalRun.getInstance().getWorldResetManager();
        World overworld = worldResetManager.getCurrentOverworld();
        World nether = worldResetManager.getCurrentNether();
        World end = worldResetManager.getCurrentEnd();

        World target;
        if (from.equals(nether) || from.equals(end)) {
            target = overworld;
        } else if (from.equals(overworld)) {
            target = cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ? nether : end;
        } else {
            // Not one of RivalRun's managed worlds (e.g. the server's own
            // default world) - leave Bukkit's default handling alone.
            return;
        }

        if (target == null || target.equals(from)) return;

        // Bukkit already computed a "to" location with the correct
        // vanilla-scaled coordinates for this dimension transition - it's
        // just pointed at the wrong world. Keep those coordinates, just
        // retarget the world; Bukkit will still search/create a portal
        // around this point using the event's search/creation radius.
        Location to = event.getTo().clone();
        to.setWorld(target);
        event.setTo(to);
    }

}
