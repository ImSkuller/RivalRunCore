package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.SimpleMenu;
import xyz.skuller.rivalRun.helpers.TeamPresets;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.TeamsManager;

import java.util.ArrayList;
import java.util.List;

// Lets admins create brand new named/colored teams beyond the fixed
// presets, and delete empty ones. Changes persist to teams.custom.
public class TeamManagerMenu extends SimpleMenu {

    public TeamManagerMenu() {
        this(null);
    }

    public TeamManagerMenu(Runnable backAction) {
        super(Rows.THREE, "§4Rival Run §0| §8Manage Teams", backAction);
    }

    @Override
    public void onSetItems() {
        clearMenu();

        TeamsManager tm = RivalRun.getInstance().getTeamManager();

        int slot = 0;
        for (Teams team : tm.getTeams()) {
            if (slot >= 18) break;
            setTeamItem(slot, team);
            slot++;
        }

        setCreateButton(22);
        addBackButton(18);
    }

    private void setTeamItem(int slot, Teams team) {
        ItemStack item = new ItemStack(TeamPresets.getWoolByColor(team.getColor()));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(team.getName().toUpperCase(), team.getColor(), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(team.getSize() + " players", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        if (team.getSize() > 0) {
            lore.add(Component.text("Remove all players before deleting", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Click to delete", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);

        setItem(slot, item, player -> {
            TeamsManager tm = RivalRun.getInstance().getTeamManager();
            if (!tm.removeCustomTeam(team)) {
                player.sendRichMessage("<red>Remove all players from that team first.");
                return;
            }
            player.sendRichMessage("<green>Deleted team " + team.getName() + ".");
            open(player);
        });
    }

    private void setCreateButton(int slot) {
        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("+ CREATE TEAM", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Click to create a new team", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);

        setItem(slot, item, player -> RivalRun.getInstance().getChatInputManager().request(player,
                Component.text("Enter the new team's name:", NamedTextColor.GOLD),
                name -> handleNameInput(player, name)));
    }

    private void handleNameInput(Player player, String name) {
        String trimmed = name.trim();
        TeamsManager tm = RivalRun.getInstance().getTeamManager();

        if (trimmed.isEmpty() || trimmed.length() > 16) {
            player.sendRichMessage("<red>Team name must be 1-16 characters.");
            new TeamManagerMenu(backAction).open(player);
            return;
        }

        boolean duplicate = tm.getTeams().stream().anyMatch(t -> t.getName().equalsIgnoreCase(trimmed));
        if (duplicate) {
            player.sendRichMessage("<red>A team with that name already exists.");
            new TeamManagerMenu(backAction).open(player);
            return;
        }

        new ColorPickerMenu(() -> new TeamManagerMenu(backAction).open(player), color -> {
            if (tm.addCustomTeam(trimmed, color)) {
                player.sendRichMessage("<green>Created team " + trimmed + ".");
            } else {
                player.sendRichMessage("<red>Could not create that team (too many teams already?).");
            }
            new TeamManagerMenu(backAction).open(player);
        }).open(player);
    }

}
