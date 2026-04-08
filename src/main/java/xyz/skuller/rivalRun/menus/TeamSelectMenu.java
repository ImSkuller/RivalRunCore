package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.SimpleMenu;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.TeamsManager;

import java.util.ArrayList;
import java.util.List;

public class TeamSelectMenu extends SimpleMenu {

    boolean fillers = RivalRun.getInstance().getConfig().getBoolean("gui.filler");
    String itemName1 = RivalRun.getInstance().getConfig().getString("gui.filler-1-name");
    String itemName2 = RivalRun.getInstance().getConfig().getString("gui.filler-2-name");



    public TeamSelectMenu() {
        super(Rows.THREE, "§4Rival Run §0| §8Team Select");
    }
    TeamsManager tm = RivalRun.getInstance().getTeamManager();

    @Override
    public void onSetItems() {

        clearMenu();

        final ItemStack filler_1 = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        final ItemMeta filler_1Meta = filler_1.getItemMeta();
        filler_1Meta.displayName(MiniMessage.miniMessage().deserialize(itemName1));
        filler_1.setItemMeta(filler_1Meta);


        final ItemStack filler_2 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        final ItemMeta filler_2Meta = filler_2.getItemMeta();
        filler_2Meta.displayName(MiniMessage.miniMessage().deserialize(itemName2));
        filler_2.setItemMeta(filler_2Meta);

        // Setting the fillers
        if (fillers) {
            setItem(0, filler_1);
            setItem(1, filler_1);
            setItem(2, filler_1);
            setItem(3, filler_2);
            setItem(4, filler_2);
            setItem(5, filler_2);
            setItem(6, filler_1);
            setItem(7, filler_2);
            setItem(8, filler_2);
            setItem(9, filler_2);
            setItem(17, filler_2);
            setItem(18, filler_2);
            setItem(19, filler_2);
            setItem(20, filler_1);
            setItem(21, filler_2);
            setItem(22, filler_2);
            setItem(23, filler_2);
            setItem(24, filler_1);
            setItem(25, filler_1);
            setItem(26, filler_1);
        }

        int slot = 10;
        for (Teams team : tm.getTeams()) {
            setTeamItem(slot, team);
            slot += 1;
        }
    }

    private void setTeamItem(int slot, Teams team) {

        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        int max = tm.getMaxTeamSize();
        int size = team.getSize();

        meta.displayName(Component.text(team.getName() + " Team", team.getColor()).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(MiniMessage.miniMessage().deserialize("<!i><#B8FFE2>Players <white>(" + size + "<!i><white>/" + max + "<!i>)"));

        for (String name : tm.getPlayerNames(team)) {
            lore.add(MiniMessage.miniMessage().deserialize("<!i><white>• " + name));
        }

        lore.add(MiniMessage.miniMessage().deserialize(" "));

        Player viewer = this.viewer;
        Teams current = tm.getPlayerTeam(viewer);

        if (current == team) {
            lore.add(Component.text("§aYou are in this team"));
        } else if (tm.isTeamFull(team)) {
            lore.add(Component.text("§cTeam is full"));
        } else {
            lore.add(Component.text("§7Click to join"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);

        setItem(slot, item, player -> {

            if (!tm.assignPlayer(player, team)) return;

            player.sendRichMessage("<green>You joined <white>" + team.getName() + "<green> Team");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

            open(player);
        });
    }
}
