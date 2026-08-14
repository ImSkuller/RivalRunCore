package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.SettingsCategories;
import xyz.skuller.rivalRun.helpers.SimpleMenu;
import xyz.skuller.rivalRun.managers.GameStateManager;

import java.util.ArrayList;
import java.util.List;

// One head per online player, opening a per-player buff/debuff editor.
// Editing is locked once the game has left WAITING - admins set handicaps
// up before a run, not mid-run.
public class BuffPlayerListMenu extends SimpleMenu {

    public BuffPlayerListMenu() {
        this(null);
    }

    public BuffPlayerListMenu(Runnable backAction) {
        super(Rows.THREE, "§4Rival Run §0| §8Player Buffs", backAction);
    }

    private boolean editable() {
        return RivalRun.getInstance().getGameStateManager().isState(GameStateManager.GameStates.WAITING);
    }

    @Override
    public void onSetItems() {
        clearMenu();

        if (!editable()) {
            setItem(4, lockBanner());
        }

        int slot = 9;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (slot >= getInventory().getSize()) break;
            setPlayerItem(slot, target);
            slot++;
        }

        addBackButton(8);
    }

    private ItemStack lockBanner() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("BUFFS ARE LOCKED", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("A game is in progress. Buffs can only", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("be edited while waiting for a new game.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    private void setPlayerItem(int slot, Player target) {
        FileConfiguration config = RivalRun.getInstance().getConfig();
        String base = "buffs." + target.getUniqueId();

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(target);
        }
        meta.displayName(Component.text(target.getName(), NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));

        int health = config.getInt(base + ".health", 0);
        int killRegen = config.getInt(base + ".killRegenSeconds", 0);
        int killStrength = config.getInt(base + ".killStrengthSeconds", 0);
        boolean hasHandicap = health != 0
                || config.getInt(base + ".saturationRate", 2) != 2
                || config.getInt(base + ".speed", 2) != 2
                || config.getInt(base + ".damageDealt", 2) != 2
                || config.getInt(base + ".damageTaken", 2) != 2
                || killRegen != 0 || killStrength != 0;

        List<Component> lore = new ArrayList<>();
        if (hasHandicap) {
            lore.add(Component.text("Health: ", NamedTextColor.GRAY).append(Component.text((health >= 0 ? "+" : "") + health, NamedTextColor.WHITE)));
            lore.add(Component.text("Kill Regen: ", NamedTextColor.GRAY).append(Component.text(killRegen + "s", NamedTextColor.WHITE)));
            lore.add(Component.text("Kill Strength: ", NamedTextColor.GRAY).append(Component.text(killStrength + "s", NamedTextColor.WHITE)));
        } else {
            lore.add(Component.text("No handicaps set", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text(editable() ? "Click to edit" : "Click to view (locked)", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);

        setItem(slot, item, player -> new SettingsCategoryMenu(
                target.getName() + "'s Buffs",
                SettingsCategories.playerBuffs(target.getUniqueId()),
                this::editable,
                "Buffs can only be edited while waiting for a new game.",
                () -> new BuffPlayerListMenu(backAction).open(player)
        ).open(player));
    }

}
