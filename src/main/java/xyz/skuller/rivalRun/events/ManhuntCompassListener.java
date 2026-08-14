package xyz.skuller.rivalRun.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.menus.ManhuntTrackerMenu;

// Right-clicking a Hunter's tracking compass opens the target picker,
// identified by its PersistentDataContainer tag (see
// ManhuntManager.isTrackerCompass) rather than just Material.COMPASS, so a
// plain vanilla compass picked up in the world doesn't misfire this.
public class ManhuntCompassListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        if (!RivalRun.getInstance().getManhuntManager().isTrackerCompass(event.getItem())) return;

        event.setUseItemInHand(Event.Result.DENY);
        new ManhuntTrackerMenu().open(player);
    }

}
