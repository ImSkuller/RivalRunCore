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
import xyz.skuller.rivalRun.helpers.SimpleMenu;
import xyz.skuller.rivalRun.helpers.TeamRole;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.SpectatorManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpectatorMenu extends SimpleMenu {

    public SpectatorMenu() {
        super(Rows.THREE, "§4Rival Run §0| §8Spectate");
    }

    @Override
    public void onSetItems() {
        clearMenu();

        TeamsManager tm = RivalRun.getInstance().getTeamManager();
        SpectatorManager sm = RivalRun.getInstance().getSpectatorManager();

        // A Manhunt Speedrunner who's died can only spectate their own
        // teammates, not the Hunters or a rival Speedrunner team - and
        // rather than teleporting freely, they're locked to that
        // teammate's position (see ManhuntListener).
        boolean restricted = RivalRun.getInstance().getGamemodeManager().isManhunt()
                && tm.getTeamRole(viewer) == TeamRole.SPEEDRUNNER;
        Iterable<Teams> visibleTeams = restricted
                ? List.of(tm.getPlayerTeam(viewer))
                : tm.getTeams();

        int slot = 0;
        outer:
        for (Teams team : visibleTeams) {
            if (team == null) continue;
            for (UUID uuid : team.getPlayers()) {
                Player target = Bukkit.getPlayer(uuid);
                if (target == null || target.equals(viewer) || sm.isSpectating(target)) continue;
                if (slot >= getInventory().getSize()) break outer;

                setTargetItem(slot, target, team, restricted);
                slot++;
            }
        }
    }

    private void setTargetItem(int slot, Player target, Teams team, boolean bindOnly) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(target);
        }

        meta.displayName(Component.text(target.getName(), team.getColor()).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Team " + team.getName(), team.getColor()).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(bindOnly ? "Click to spectate" : "Click to teleport", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);

        setItem(slot, item, player -> {
            if (bindOnly) {
                RivalRun.getInstance().getManhuntManager().bindSpectatorTo(player, target);
                player.sendRichMessage("<red>You are now spectating <white>" + target.getName() + "<red>.");
            } else {
                player.teleport(target.getLocation());
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            player.closeInventory();
        });
    }

}
