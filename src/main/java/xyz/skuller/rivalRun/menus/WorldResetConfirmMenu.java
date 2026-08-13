package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.SimpleMenu;

import java.util.List;

// A confirm/cancel gate in front of WorldResetManager.resetWorlds() - this
// regenerates the Overworld/Nether/End with a new seed and removes the old
// ones, so it needs an explicit, unmistakable confirmation step.
public class WorldResetConfirmMenu extends SimpleMenu {

    public WorldResetConfirmMenu() {
        super(Rows.THREE, "§4Rival Run §0| §cReset World?");
    }

    @Override
    public void onSetItems() {
        clearMenu();

        boolean deletes = RivalRun.getInstance().getConfig().getBoolean("worldReset.deleteOldWorlds", true);

        ItemStack warning = new ItemStack(Material.TNT);
        ItemMeta warningMeta = warning.getItemMeta();
        warningMeta.displayName(Component.text("This regenerates the entire world!", NamedTextColor.RED, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        warningMeta.lore(List.of(
                Component.text("A brand new Overworld, Nether, and End", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("will be generated with a random seed.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(" ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Everyone will be teleported there, and", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("the game will be fully reset.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(" ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                deletes
                        ? Component.text("The current world will be PERMANENTLY DELETED.", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
                        : Component.text("The current world will be archived (not deleted).", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
        ));
        warning.setItemMeta(warningMeta);
        setItem(13, warning);

        ItemStack confirm = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(Component.text("CONFIRM", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        confirmMeta.lore(List.of(Component.text("Click to regenerate the world now", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        confirm.setItemMeta(confirmMeta);

        setItem(20, confirm, player -> {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.4f);
            RivalRun.getInstance().getWorldResetManager().resetWorlds();
        });

        ItemStack cancel = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("CANCEL", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        cancelMeta.lore(List.of(Component.text("Click to back out, nothing happens", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        cancel.setItemMeta(cancelMeta);

        setItem(24, cancel, player -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            player.closeInventory();
        });
    }

}
