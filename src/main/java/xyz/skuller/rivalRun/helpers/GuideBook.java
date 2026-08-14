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

                        <gray>Classic: teams race to beat the game. First to kill the Ender Dragon wins - or be the last team standing.

                        <gray>Manhunt: Hunters chase down one or two Speedrunner teams."""),
                page("""
                        <gold><bold>Getting Started

                        <gray>Pick a team from the menu that opens on join, or run:
                        <white>/rr team select

                        <gray>Switch or leave with:
                        <white>/rr team switch
                        <white>/rr team leave"""),
                page("""
                        <gold><bold>During the Run

                        <gray>Check the sidebar for your team/role, the timer, and standings.

                        <gray>Classic: a grace period blocks PvP at the start.
                        <gray>Manhunt: Hunters are frozen for a headstart instead - no grace period.

                        <gray>Achievements announce in chat as milestones are reached."""),
                page("""
                        <gold><bold>Manhunt

                        <gray>Hunters get a tracking compass - right-click it to pick who to follow, and respawn freely.

                        <gray>Speedrunners don't respawn - dying makes you a (team-only) spectator. If your whole team falls you join the Hunters at half health.

                        <gray>While your team is the only Speedrunner team left, revive a fallen teammate once each with:
                        <white>/rr team revive <player>"""),
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

                        <gray>Run <white>/rr<gray> with no arguments for the Admin Menu, or use the commands on the following pages. Every command still works in its old flat form too (e.g. <white>/rr start<gray>)."""),
                page("""
                        <gold><bold>Game Control

                        <white>/rr game start
                        <gray>Begin the countdown

                        <white>/rr game pause<gray> / <white>resume
                        <gray>Freeze/unfreeze

                        <white>/rr game end
                        <gray>End with no winner

                        <white>/rr game reset
                        <gray>Reset teams & state (the map stays the same)"""),
                page("""
                        <gold><bold>Teams

                        <white>/rr team lock<gray> / <white>unlock
                        <gray>Stop/allow joining or switching

                        <gray>Create custom Classic teams via Settings > Manage Teams in the Admin Menu."""),
                page("""
                        <gold><bold>Gamemode

                        <white>/rr admin gamemode <classic|manhunt>
                        <gray>Switch ruleset - only while WAITING. Resets teams/state and reopens team select for everyone.

                        <gray>Manhunt team/headstart settings live in Settings > Manhunt."""),
                page("""
                        <gold><bold>Spectators & Buffs

                        <white>/rr admin spectator [player]
                        <gray>Force spectate on/off

                        <white>/rr admin buffs
                        <gray>Per-player health, speed, damage & kill-reward handicaps. Locked once a game starts."""),
                page("""
                        <gold><bold>World & Settings

                        <white>/rr admin resetworld
                        <gray>Fresh Overworld/Nether/End, new seed. Confirm/cancel GUI.

                        <white>/rr admin settings
                        <gray>Every config value is editable in-game - no file editing needed."""),
                page("""
                        <gold><bold>Tips

                        <gray>Set buffs and check Settings before you /rr game start - both lock once the game begins.

                        <gray>/rr game reset never touches the map. Use /rr admin resetworld for that.

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
