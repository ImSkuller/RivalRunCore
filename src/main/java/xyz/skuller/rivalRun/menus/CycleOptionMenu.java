package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.SettingEntry;
import xyz.skuller.rivalRun.helpers.SimpleMenu;

import java.util.List;

// A CYCLE SettingEntry's picker - every option is shown at once so you can
// jump straight to the one you want, instead of repeatedly clicking to
// step through them one at a time.
public class CycleOptionMenu extends SimpleMenu {

    private final SettingEntry entry;

    public CycleOptionMenu(SettingEntry entry, Runnable backAction) {
        super(Rows.ONE, "§4Rival Run §0| §8" + entry.label(), backAction);
        this.entry = entry;
    }

    @Override
    public void onSetItems() {
        clearMenu();

        String[] options = entry.cycleOptions();
        int current = RivalRun.getInstance().getConfig().getInt(entry.configPath(), entry.defaultCycleIndex());

        for (int i = 0; i < options.length && i < 8; i++) {
            setOptionItem(i, i, options[i], i == current);
        }

        addBackButton(8);
    }

    private void setOptionItem(int slot, int index, String label, boolean selected) {
        ItemStack item = new ItemStack(selected ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(label, selected ? NamedTextColor.GREEN : NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text(selected ? "Currently selected" : "Click to select", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);

        setItem(slot, item, player -> {
            RivalRun plugin = RivalRun.getInstance();
            plugin.getConfig().set(entry.configPath(), index);
            plugin.saveConfig();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            if (backAction != null) backAction.run();
        });
    }

}
