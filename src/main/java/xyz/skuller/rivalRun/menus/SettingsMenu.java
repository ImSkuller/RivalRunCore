package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.helpers.SettingEntry;
import xyz.skuller.rivalRun.helpers.SettingsCategories;
import xyz.skuller.rivalRun.helpers.SimpleMenu;

import java.util.List;

// Top-level picker for the in-game settings editor - one button per
// config.yml section, each opening a SettingsCategoryMenu.
public class SettingsMenu extends SimpleMenu {

    public SettingsMenu() {
        super(Rows.THREE, "§4Rival Run §0| §8Settings");
    }

    @Override
    public void onSetItems() {
        clearMenu();

        setCategory(10, Material.LIME_DYE, "Commands", SettingsCategories.commands());
        setCategory(11, Material.CHEST, "GUI", SettingsCategories.gui());
        setCategory(12, Material.CLOCK, "Grace Period", SettingsCategories.grace());
        setCategory(13, Material.PLAYER_HEAD, "Teams", SettingsCategories.teams());
        setCategory(14, Material.OAK_SIGN, "Scoreboard", SettingsCategories.scoreboard());
        setCategory(15, Material.BOOK, "Tab List", SettingsCategories.tablist());
        setCategory(16, Material.KNOWLEDGE_BOOK, "MOTD", SettingsCategories.motd());
        setCategory(19, Material.COMPASS, "Spectator", SettingsCategories.spectator());
        setCategory(20, Material.GOLDEN_APPLE, "Achievements", SettingsCategories.achievements());
        setCategory(21, Material.WRITABLE_BOOK, "Messages", SettingsCategories.messages());

        setItem(24, teamManagerIcon(), player -> new TeamManagerMenu().open(player));
    }

    private void setCategory(int slot, Material icon, String label, List<SettingEntry> entries) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(label, NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Click to open", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);

        setItem(slot, item, player -> new SettingsCategoryMenu(label, entries).open(player));
    }

    private ItemStack teamManagerIcon() {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Manage Teams", NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Create or delete custom teams", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

}
