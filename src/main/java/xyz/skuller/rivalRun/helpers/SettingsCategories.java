package xyz.skuller.rivalRun.helpers;

import org.bukkit.Material;

import java.util.List;

import static xyz.skuller.rivalRun.helpers.SettingEntry.cycle;
import static xyz.skuller.rivalRun.helpers.SettingEntry.text;
import static xyz.skuller.rivalRun.helpers.SettingEntry.toggle;

// Builds the list of editable settings shown per category in the settings
// GUI. Mirrors config.yml's section layout one-for-one.
public class SettingsCategories {

    public static List<SettingEntry> commands() {
        return List.of(
                toggle(Material.LIME_DYE, "End Game Command", "Allow /rivalrun end", "commands.endGame"),
                toggle(Material.LIME_DYE, "Pause Game Command", "Allow /rivalrun pause & resume", "commands.pauseGame"),
                toggle(Material.LIME_DYE, "Team Select Command", "Allow /rivalrun select & switch", "commands.teamSelect"),
                toggle(Material.LIME_DYE, "Team Leave Command", "Allow /rivalrun leave", "commands.teamLeave"),
                toggle(Material.REDSTONE, "Debug Commands", "Allow /rrdebug", "commands.debug")
        );
    }

    public static List<SettingEntry> gui() {
        return List.of(
                toggle(Material.GLASS_PANE, "GUI Fillers", "Decorative glass panes in menus", "gui.filler"),
                text(Material.WHITE_STAINED_GLASS_PANE, "Filler 1 Name", "MiniMessage name for the first filler", "gui.filler-1-name"),
                text(Material.RED_STAINED_GLASS_PANE, "Filler 2 Name", "MiniMessage name for the second filler", "gui.filler-2-name"),
                toggle(Material.CHEST, "Admin Menu", "Open the admin menu on /rivalrun with no args", "gui.adminMenu")
        );
    }

    public static List<SettingEntry> grace() {
        return List.of(
                toggle(Material.SHIELD, "Grace Period", "PvP-disabled prep phase at run start", "grace.enabled"),
                text(Material.CLOCK, "Grace Period Length", "Seconds, minimum 60", "grace.gracePeriod")
        );
    }

    public static List<SettingEntry> teams() {
        return List.of(
                text(Material.PLAYER_HEAD, "Team Count", "2 to 7 - applies on next reset", "teams.list"),
                text(Material.ARMOR_STAND, "Max Team Size", "2 to 6 - applies on next reset", "teams.max"),
                toggle(Material.DIAMOND_SWORD, "Friendly Fire", "Let teammates damage each other", "teams.friendlyFire"),
                toggle(Material.ENDER_PEARL, "Team Switching", "Allow switching teams mid-game", "teams.teamSwitching"),
                toggle(Material.CHEST, "Login GUI", "Open team select automatically on join", "teams.loginGUI"),
                cycle(Material.NAME_TAG, "Team Name Preset", "Applies on next reset", "teams.teamName",
                        "Default (colors)", "Passive Mobs", "Hostile Mobs", "Flowers", "Blocks")
        );
    }

    public static List<SettingEntry> scoreboard() {
        return List.of(
                toggle(Material.OAK_SIGN, "Scoreboard", "Show the sidebar scoreboard", "scoreboard.enabled"),
                text(Material.PAPER, "Scoreboard Title", "MiniMessage title text", "scoreboard.title")
        );
    }

    public static List<SettingEntry> tablist() {
        return List.of(
                toggle(Material.BOOK, "Tab List", "Show the custom tab list header/footer", "tablist.enabled"),
                text(Material.WRITABLE_BOOK, "Tab List Header", "Placeholders: {state} {timer} {online} {max}", "tablist.header"),
                text(Material.WRITABLE_BOOK, "Tab List Footer", "Placeholders: {state} {timer} {online} {max}", "tablist.footer")
        );
    }

    public static List<SettingEntry> motd() {
        return List.of(
                toggle(Material.KNOWLEDGE_BOOK, "Dynamic MOTD", "Reflect game state in the server list", "motd.enabled"),
                text(Material.PAPER, "MOTD - Waiting", "Placeholders: {online} {max} {timer}", "motd.waiting"),
                text(Material.PAPER, "MOTD - Starting", "Placeholders: {online} {max} {timer}", "motd.starting"),
                text(Material.PAPER, "MOTD - Running", "Placeholders: {online} {max} {timer}", "motd.running"),
                text(Material.PAPER, "MOTD - Post-game", "Placeholders: {online} {max} {timer}", "motd.post")
        );
    }

    public static List<SettingEntry> spectator() {
        return List.of(
                toggle(Material.COMPASS, "Teleport Menu", "Give spectators a teleport compass", "spectator.teleportMenu"),
                toggle(Material.BEACON, "Elimination Win", "Last team standing auto-wins", "spectator.eliminationWin")
        );
    }

    public static List<SettingEntry> achievements() {
        return List.of(
                toggle(Material.GOLDEN_APPLE, "Achievements", "Speedrun milestone achievements", "achievements.enabled")
        );
    }

    public static List<SettingEntry> worldReset() {
        return List.of(
                toggle(Material.TNT, "Delete Old Worlds", "Off = archive instead of deleting on /rivalrun resetworld", "worldReset.deleteOldWorlds")
        );
    }

    public static List<SettingEntry> messages() {
        return List.of(
                text(Material.PAPER, "Team Full", "Shown when trying to join a full team", "messages.teamFull"),
                text(Material.PAPER, "Teams Locked", "Shown when teams are locked", "messages.teamsLocked"),
                text(Material.PAPER, "Not In Team", "Shown when an action needs a team", "messages.notInTeam"),
                text(Material.PAPER, "Team Win", "Placeholder: {team}", "messages.teamWin"),
                text(Material.PAPER, "Spectator Enabled", "Shown to a player moved into spectator mode", "messages.spectatorEnabled"),
                text(Material.PAPER, "Spectator Disabled", "Shown to a player moved out of spectator mode", "messages.spectatorDisabled")
        );
    }

}
