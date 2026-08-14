package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.helpers.SimpleMenu;

import java.util.List;
import java.util.function.Consumer;

// Reusable 16-color picker - one colored wool pane per NamedTextColor,
// handing the chosen color to a callback.
public class ColorPickerMenu extends SimpleMenu {

    private static final NamedTextColor[] COLORS = {
            NamedTextColor.BLACK, NamedTextColor.DARK_BLUE, NamedTextColor.DARK_GREEN, NamedTextColor.DARK_AQUA,
            NamedTextColor.DARK_RED, NamedTextColor.DARK_PURPLE, NamedTextColor.GOLD, NamedTextColor.GRAY,
            NamedTextColor.DARK_GRAY, NamedTextColor.BLUE, NamedTextColor.GREEN, NamedTextColor.AQUA,
            NamedTextColor.RED, NamedTextColor.LIGHT_PURPLE, NamedTextColor.YELLOW, NamedTextColor.WHITE
    };

    private final Consumer<NamedTextColor> onPick;

    public ColorPickerMenu(Runnable backAction, Consumer<NamedTextColor> onPick) {
        super(Rows.THREE, "§4Rival Run §0| §8Pick a Color", backAction);
        this.onPick = onPick;
    }

    @Override
    public void onSetItems() {
        clearMenu();

        int slot = 9;
        for (NamedTextColor color : COLORS) {
            setColorItem(slot, color);
            slot++;
        }

        addBackButton(26);
    }

    private void setColorItem(int slot, NamedTextColor color) {
        ItemStack item = new ItemStack(iconFor(color));
        ItemMeta meta = item.getItemMeta();

        String name = NamedTextColor.NAMES.key(color);
        String display = (name != null ? name : "color").replace('_', ' ').toUpperCase();
        meta.displayName(Component.text(display, color, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Click to select", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);

        setItem(slot, item, player -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onPick.accept(color);
        });
    }

    // Minecraft's 16 dye colors and the 16 NamedTextColors are two different
    // palettes with no clean 1:1 mapping - this is a best-effort pairing.
    // The colored, exact-name label on each item is the real identifier.
    private Material iconFor(NamedTextColor color) {
        if (color == NamedTextColor.BLACK) return Material.BLACK_DYE;
        if (color == NamedTextColor.DARK_BLUE) return Material.BLUE_DYE;
        if (color == NamedTextColor.DARK_GREEN) return Material.GREEN_DYE;
        if (color == NamedTextColor.DARK_AQUA) return Material.CYAN_DYE;
        if (color == NamedTextColor.DARK_RED) return Material.BROWN_DYE;
        if (color == NamedTextColor.DARK_PURPLE) return Material.PURPLE_DYE;
        if (color == NamedTextColor.GOLD) return Material.ORANGE_DYE;
        if (color == NamedTextColor.GRAY) return Material.LIGHT_GRAY_DYE;
        if (color == NamedTextColor.DARK_GRAY) return Material.GRAY_DYE;
        if (color == NamedTextColor.BLUE) return Material.LIGHT_BLUE_DYE;
        if (color == NamedTextColor.GREEN) return Material.LIME_DYE;
        if (color == NamedTextColor.AQUA) return Material.CYAN_DYE;
        if (color == NamedTextColor.RED) return Material.RED_DYE;
        if (color == NamedTextColor.LIGHT_PURPLE) return Material.MAGENTA_DYE;
        if (color == NamedTextColor.YELLOW) return Material.YELLOW_DYE;
        return Material.WHITE_DYE;
    }

}
