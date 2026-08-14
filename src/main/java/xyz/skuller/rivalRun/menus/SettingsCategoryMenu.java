package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.SettingEntry;
import xyz.skuller.rivalRun.helpers.SimpleMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// Renders one config section as a list of clickable, live-editable settings.
// TOGGLE flips a boolean in place; CYCLE advances through a fixed option
// list; TEXT/NUMBER open a chat prompt for a free-form string or an
// always-parsed-as-int value respectively.
//
// Optionally gated behind an editableCheck (e.g. "only while the game is
// still WAITING") - clicking any entry while locked shows lockedMessage
// instead of opening the toggle/cycle/chat-prompt flow. Defaults to always
// editable, so existing config-section menus are unaffected.
public class SettingsCategoryMenu extends SimpleMenu {

    private final List<SettingEntry> entries;
    private final Supplier<Boolean> editableCheck;
    private final String lockedMessage;

    public SettingsCategoryMenu(String title, List<SettingEntry> entries, Runnable backAction) {
        this(title, entries, null, null, backAction);
    }

    public SettingsCategoryMenu(String title, List<SettingEntry> entries, Supplier<Boolean> editableCheck, String lockedMessage, Runnable backAction) {
        super(Rows.ONE, "§4Rival Run §0| §8" + title, backAction);
        this.entries = entries;
        this.editableCheck = editableCheck;
        this.lockedMessage = lockedMessage;
    }

    private boolean isLocked() {
        return editableCheck != null && !editableCheck.get();
    }

    @Override
    public void onSetItems() {
        clearMenu();

        int slot = 0;
        for (SettingEntry entry : entries) {
            setEntryItem(slot++, entry);
        }

        addBackButton(8);
    }

    private void setEntryItem(int slot, SettingEntry entry) {
        FileConfiguration config = RivalRun.getInstance().getConfig();
        boolean locked = isLocked();

        ItemStack item = new ItemStack(entry.icon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(entry.label(), NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(entry.description(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Current: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(currentValueDisplay(config, entry), NamedTextColor.WHITE)));

        if (locked) {
            lore.add(Component.text("🔒 Locked", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        } else {
            String hint = switch (entry.type()) {
                case TOGGLE -> "Click to toggle";
                case CYCLE -> "Click to pick a value";
                case TEXT, NUMBER -> "Click to edit in chat";
            };
            lore.add(Component.text(hint, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        item.setItemMeta(meta);

        setItem(slot, item, player -> handleClick(player, entry));
    }

    private String currentValueDisplay(FileConfiguration config, SettingEntry entry) {
        return switch (entry.type()) {
            case TOGGLE -> String.valueOf(config.getBoolean(entry.configPath()));
            case CYCLE -> {
                int index = config.getInt(entry.configPath(), entry.defaultCycleIndex());
                String[] options = entry.cycleOptions();
                yield (index >= 0 && index < options.length) ? options[index] : String.valueOf(index);
            }
            case NUMBER -> String.valueOf(config.getInt(entry.configPath()));
            case TEXT -> String.valueOf(config.get(entry.configPath()));
        };
    }

    private void handleClick(Player player, SettingEntry entry) {
        if (isLocked()) {
            player.sendMessage(Component.text(lockedMessage, NamedTextColor.RED));
            return;
        }

        RivalRun plugin = RivalRun.getInstance();
        FileConfiguration config = plugin.getConfig();

        switch (entry.type()) {
            case TOGGLE -> {
                config.set(entry.configPath(), !config.getBoolean(entry.configPath()));
                plugin.saveConfig();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                open(player);
            }
            case CYCLE -> new CycleOptionMenu(entry, () -> open(player)).open(player);
            case TEXT, NUMBER -> plugin.getChatInputManager().request(player,
                    Component.text("Enter a new value for " + entry.label() + ":", NamedTextColor.GOLD),
                    input -> applyTextValue(player, entry, input));
        }
    }

    private void applyTextValue(Player player, SettingEntry entry, String input) {
        RivalRun plugin = RivalRun.getInstance();
        FileConfiguration config = plugin.getConfig();

        Object newValue;

        if (entry.type() == SettingEntry.SettingType.NUMBER) {
            try {
                newValue = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("That must be a whole number.", NamedTextColor.RED));
                open(player);
                return;
            }
        } else {
            newValue = input.replace("\\n", "\n");
        }

        config.set(entry.configPath(), newValue);
        plugin.saveConfig();
        player.sendMessage(Component.text(entry.label() + " updated.", NamedTextColor.GREEN));
        open(player);
    }

}
