package xyz.skuller.rivalRun.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import xyz.skuller.rivalRun.helpers.Menu;


public class InventoryListener implements Listener {

    @EventHandler
    public void OnClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        final Inventory clickedInventory = event.getClickedInventory();

        // If Clicked Inventory Does Not Have An Item/ Button
        if (clickedInventory == null) return;

        // If Clicked Inventory Is Not Menu
        if (!(clickedInventory.getHolder() instanceof final Menu menu)) return;

        // If Clicked Inventory Is A Custom Inventory From The Plugin
        event.setCancelled(true);
        menu.click((Player) event.getWhoClicked(), event.getSlot());

    }

}

