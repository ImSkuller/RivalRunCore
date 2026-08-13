package xyz.skuller.rivalRun.helpers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

// Two written-book guides, opened directly via Player#openBook (never added
// to an inventory) - one for players, one covering the full admin toolset.
// /rivalrun help shows whichever fits the sender's permissions.
public class GuideBook {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static void openPlayerGuide(Player player) {
        player.openBook(build("Rival Run Guide", List.of(
                page("""
                        <gold><bold>Rival Run

                        <gray>Teams race to beat the game. First team to kill the Ender Dragon wins - or be the last team standing."""),
                page("""
                        <gold><bold>Getting Started

                        <gray>Pick a team from the menu that opens on join, or run:
                        <white>/rr select

                        <gray>Switch or leave with:
                        <white>/rr switch
                        <white>/rr leave"""),
                page("""
                        <gold><bold>During the Run

                        <gray>Check the sidebar for your team, the timer, and standings.

                        <gray>A grace period blocks PvP at the start - watch the action bar.

                        <gray>Achievements announce in chat as teams reach milestones."""),
                page("""
                        <gold><bold>Spectating

                        <gray>No team when the game starts? You'll spectate automatically.

                        <gray>Use the Spectate compass, or run:
                        <white>/rr spectate

                        <gray>to teleport to any remaining player."""),
                page("""
                        <gold><bold>After the Game

                        <gray>Run:
                        <white>/rr summary

                        <gray>to see the winner, run time, and each team's achievements from the last match.""")
        )));
    }

    public static void openAdminGuide(Player player) {
        player.openBook(build("Rival Run Admin Guide", List.of(
                page("""
                        <gold><bold>Rival Run - Admin Guide

                        <gray>Run <white>/rr<gray> with no arguments for the Admin Menu, or use the commands on the following pages."""),
                page("""
                        <gold><bold>Game Control

                        <white>/rr start
                        <gray>Begin the countdown

                        <white>/rr pause<gray> / <white>resume
                        <gray>Freeze/unfreeze

                        <white>/rr end
                        <gray>End with no winner

                        <white>/rr reset
                        <gray>Reset teams & state (the map stays the same)"""),
                page("""
                        <gold><bold>Teams

                        <white>/rr lock<gray> / <white>unlock
                        <gray>Stop/allow joining or switching

                        <gray>Create custom teams via Settings > Manage Teams in the Admin Menu."""),
                page("""
                        <gold><bold>Spectators & Buffs

                        <white>/rr spectator [player]
                        <gray>Force spectate on/off

                        <white>/rr buffs
                        <gray>Per-player health, speed, damage & kill-reward handicaps. Locked once a game starts."""),
                page("""
                        <gold><bold>World & Settings

                        <white>/rr resetworld
                        <gray>Fresh Overworld/Nether/End, new seed. Confirm/cancel GUI.

                        <gray>Every config value is editable in Settings - no file editing needed."""),
                page("""
                        <gold><bold>Tips

                        <gray>Set buffs and check Settings before you /rr start - both lock once the game begins.

                        <gray>/rr reset never touches the map. Use /rr resetworld for that.

                        <gray>/rr summary shows the last completed match's results to everyone, any time.""")
        )));
    }

    private static Component page(String miniMessage) {
        return MM.deserialize(miniMessage);
    }

    private static ItemStack build(String title, List<Component> pages) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.title(Component.text(title));
        meta.author(Component.text("Rival Run"));
        meta.pages(pages);
        item.setItemMeta(meta);
        return item;
    }

}
