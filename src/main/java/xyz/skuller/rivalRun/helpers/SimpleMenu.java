package xyz.skuller.rivalRun.helpers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class SimpleMenu implements Menu {
    protected Player viewer;

    // Set when this menu instance was opened from another menu; reopening it
    // (e.g. via addBackButton()) returns to that menu. Null for menus that
    // are only ever opened as a top-level entry point (commands/events).
    protected final Runnable backAction;

    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();
    private final Inventory inventory;

    // Menu titles are written as legacy "§"-coded strings throughout the
    // codebase; deserializing here means every existing subclass keeps
    // working unchanged while still using the non-deprecated Component
    // overload of createInventory.
    public SimpleMenu(Rows rows, String title) {
        this(rows, title, null);
    }

    public SimpleMenu(Rows rows, String title, Runnable backAction) {

        this.backAction = backAction;
        this.inventory = Bukkit.createInventory(this, rows.getSize(),
                LegacyComponentSerializer.legacySection().deserialize(title));

    }

    @Override
    public void open(Player player) {

        this.viewer = player;

        clearMenu();
        onSetItems();

        player.openInventory(getInventory());
    }

    @Override
    public void click(Player player, int slot) {
        final Consumer<Player> action = this.actions.get(slot);

        if (action != null) {
            action.accept(player);
        }

    }

    @Override
    public void setItem(int slot, ItemStack item) {
        setItem(slot, item, player -> {});

    }

    @Override
    public void setItem(int slot, ItemStack item, Consumer<Player> action) {

        this.actions.put(slot, action);

        getInventory().setItem(slot, item);

    }

    protected void clearMenu() {
        actions.clear();
        inventory.clear();
    }

    // Subclasses that can be reached from another menu call this (with a
    // slot free in their layout) inside onSetItems() to render a back
    // button. Does nothing if this instance wasn't given a backAction, i.e.
    // it was opened directly from a command/event rather than another menu.
    protected void addBackButton(int slot) {
        if (backAction == null) return;

        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Back", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Return to the previous menu", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);

        setItem(slot, item, player -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            backAction.run();
        });
    }

    public abstract void onSetItems();

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public enum Rows {

        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6);

        private final int size;

        Rows(int rows) {
            this.size = rows * 9;
        }

        public int getSize() {
            return size;
        }

    }

}
