package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.AchievementType;
import xyz.skuller.rivalRun.helpers.SimpleMenu;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.SpectatorManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

import java.util.ArrayList;
import java.util.List;

// A Hunter's compass menu - lets them pick which alive Speedrunner to
// track (across every Speedrunner team). Unlike SpectatorMenu, clicking
// sets a live-following compass target rather than teleporting.
public class ManhuntTrackerMenu extends SimpleMenu {

    public ManhuntTrackerMenu() {
        super(Rows.THREE, "§4Rival Run §0| §8Track a Speedrunner");
    }

    @Override
    public void onSetItems() {
        clearMenu();

        TeamsManager tm = RivalRun.getInstance().getTeamManager();
        SpectatorManager sm = RivalRun.getInstance().getSpectatorManager();

        int slot = 0;
        for (Teams team : tm.getSpeedrunnerTeams()) {
            for (String name : tm.getPlayerNames(team)) {
                Player target = Bukkit.getPlayer(name);
                if (target == null || sm.isSpectating(target)) continue;
                if (slot >= getInventory().getSize()) break;

                setTargetItem(slot, target, team);
                slot++;
            }
        }
    }

    private void setTargetItem(int slot, Player target, Teams team) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(target);
        }

        meta.displayName(Component.text(target.getName(), team.getColor()).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Team " + team.getName(), team.getColor()).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Click to track", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);

        setItem(slot, item, player -> {
            RivalRun.getInstance().getManhuntManager().setTarget(player, target);
            player.sendRichMessage("<red>Your compass is now tracking <white>" + target.getName() + "<red>.");
            player.playSound(player.getLocation(), Sound.BLOCK_LODESTONE_PLACE, 1f, 1f);
            player.closeInventory();

            Teams hunterTeam = RivalRun.getInstance().getTeamManager().getPlayerTeam(player);
            if (hunterTeam != null) {
                RivalRun.getInstance().getAchievementManager().award(hunterTeam, AchievementType.SHARP_EYE);
            }
        });
    }

}
