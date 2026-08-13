package xyz.skuller.rivalRun.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.menus.SpectatorMenu;

public class SpectatorCompassListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        if (event.getItem() == null || event.getItem().getType() != Material.COMPASS) return;
        if (!RivalRun.getInstance().getSpectatorManager().isSpectating(player)) return;

        event.setUseItemInHand(Event.Result.DENY);
        new SpectatorMenu().open(player);
    }

}
