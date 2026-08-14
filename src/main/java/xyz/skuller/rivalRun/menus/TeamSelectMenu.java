package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.GuideBook;
import xyz.skuller.rivalRun.helpers.Messages;
import xyz.skuller.rivalRun.helpers.SimpleMenu;
import xyz.skuller.rivalRun.helpers.TeamPresets;
import xyz.skuller.rivalRun.helpers.TeamRole;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.TeamsManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
            setItem(6, filler_1);
            setItem(7, filler_2);
            setItem(8, filler_2);
            setItem(9, filler_2);
            setItem(17, filler_2);
            setItem(18, filler_2);
            setItem(19, filler_2);
            setItem(20, filler_1);
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

        setLeaveButton(4);
        setRandomTeamButton(22);
        setHelpButton(5);
    }

    private void setTeamItem(int slot, Teams team) {

        Material icon = team.getRole() == TeamRole.HUNTER
                ? Material.BOW
                : TeamPresets.getWoolByColor(team.getColor());
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        int max = tm.getMaxTeamSize();
        int size = team.getSize();

        String label = team.getRole() == TeamRole.HUNTER ? team.getName() : team.getName() + " Team";
        meta.displayName(Component.text(label.toUpperCase(), team.getColor(), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(MiniMessage.miniMessage().deserialize("<!i><#B8FFE2>Players <white>(" + size + "<!i><white>/" + max + "<!i>)"));

        for (String name : tm.getPlayerNames(team)) {
            lore.add(MiniMessage.miniMessage().deserialize("<!i><white>• " + name));
        }

        lore.add(MiniMessage.miniMessage().deserialize(" "));

        Player viewer = this.viewer;
        Teams current = tm.getPlayerTeam(viewer);

        if (current == team) {
            lore.add(Component.text("You are in this team", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        } else if (tm.isTeamFull(team)) {
            lore.add(Component.text("Team is full", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Click to join", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
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

    private void setLeaveButton(int slot) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Leave Team", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Click to leave your current team", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);

        setItem(slot, item, player -> {
            if (!tm.isInTeam(player)) {
                player.sendMessage(Messages.get("messages.notInTeam"));
                return;
            }

            tm.removePlayer(player);
            player.sendRichMessage("<green>You have left your team.");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            open(player);
        });
    }

    private void setHelpButton(int slot) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Help", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Click to open the guide book", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);

        setItem(slot, item, GuideBook::openPlayerGuide);
    }

    private void setRandomTeamButton(int slot) {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Random Team", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Joins whichever team has the most room", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);

        setItem(slot, item, player -> {
            Optional<Teams> smallest = tm.getTeams().stream()
                    .filter(t -> !tm.isTeamFull(t))
                    .min(Comparator.comparingInt(Teams::getSize));

            if (smallest.isEmpty()) {
                player.sendRichMessage("<red>All teams are full!");
                return;
            }

            if (!tm.assignPlayer(player, smallest.get())) return;

            player.sendRichMessage("<green>You joined <white>" + smallest.get().getName() + "<green> Team");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            open(player);
        });
    }
}
