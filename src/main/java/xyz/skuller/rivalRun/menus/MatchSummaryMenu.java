package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.AchievementType;
import xyz.skuller.rivalRun.helpers.MatchSummary;
import xyz.skuller.rivalRun.helpers.SimpleMenu;
import xyz.skuller.rivalRun.helpers.TeamPresets;

import java.util.ArrayList;
import java.util.List;

// Read-only recap of the most recently completed match - winner, run time,
// and each team's survivors/achievements. Reads WinManager's frozen
// snapshot rather than live team/achievement state, since both get wiped
// by the next reset. Open to everyone, not just admins.
public class MatchSummaryMenu extends SimpleMenu {

    public MatchSummaryMenu() {
        this(null);
    }

    public MatchSummaryMenu(Runnable backAction) {
        super(Rows.THREE, "§4Rival Run §0| §8Match Summary", backAction);
    }

    @Override
    public void onSetItems() {
        clearMenu();

        MatchSummary summary = RivalRun.getInstance().getWinManager().getLastSummary();

        if (summary == null) {
            setItem(13, noSummaryItem());
            addBackButton(18);
            return;
        }

        setItem(4, headerItem(summary));

        int slot = 10;
        for (MatchSummary.TeamResult result : summary.teamResults()) {
            if (slot >= getInventory().getSize()) break;
            setTeamItem(slot, result, summary.winningTeamName());
            slot++;
        }

        addBackButton(18);
    }

    private ItemStack noSummaryItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("No match completed yet", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Play a game to see a recap here.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack headerItem(MatchSummary summary) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text((summary.winningTeamName() + " Wins!").toUpperCase(), summary.winningTeamColor(), TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(summary.reason().getDescription(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Run time: ", NamedTextColor.GRAY)
                .append(Component.text(summary.runTime(), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void setTeamItem(int slot, MatchSummary.TeamResult result, String winningTeamName) {
        boolean won = result.name().equals(winningTeamName);

        ItemStack item = new ItemStack(TeamPresets.getWoolByColor(result.color()));
        ItemMeta meta = item.getItemMeta();

        Component name = Component.text(result.name().toUpperCase(), result.color(), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false);
        if (won) name = name.append(Component.text(" 🏆", NamedTextColor.GOLD));
        meta.displayName(name);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Survived: ", NamedTextColor.GRAY)
                .append(Component.text(result.alive() + "/" + result.total(), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());

        if (result.achievements().isEmpty()) {
            lore.add(Component.text("No achievements", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Achievements:", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            for (AchievementType type : result.achievements()) {
                lore.add(Component.text("⭐ " + type.getDisplayName(), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            }
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        setItem(slot, item);
    }

}
