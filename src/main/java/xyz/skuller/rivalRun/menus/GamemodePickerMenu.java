package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Gamemode;
import xyz.skuller.rivalRun.helpers.SimpleMenu;

import java.util.List;

// Lets an admin pick Classic or Manhunt from a menu instead of a single
// toggle button. Switching only takes effect while the game is WAITING -
// GamemodeManager.switchTo() enforces that and messages the player if not.
public class GamemodePickerMenu extends SimpleMenu {

    public GamemodePickerMenu(Runnable backAction) {
        super(Rows.ONE, "§4Rival Run §0| §8Pick a Gamemode", backAction);
    }

    @Override
    public void onSetItems() {
        clearMenu();

        setModeItem(2, Gamemode.CLASSIC, Material.DIAMOND_SWORD,
                "Teams race to beat the game, or be the last team standing.");
        setModeItem(6, Gamemode.MANHUNT, Material.BOW,
                "Hunters chase down 1-2 Speedrunner teams.");

        addBackButton(8);
    }

    private void setModeItem(int slot, Gamemode mode, Material icon, String description) {
        boolean active = RivalRun.getInstance().getGamemodeManager().getActive() == mode;

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(mode.name(), active ? NamedTextColor.GREEN : NamedTextColor.WHITE, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(description, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text(active ? "Currently active" : "Click to switch - only while WAITING", active ? NamedTextColor.GREEN : NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);

        setItem(slot, item, (Player player) -> {
            if (active) return;

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            RivalRun.getInstance().getGamemodeManager().switchTo(mode, player);
            open(player);
        });
    }

}
