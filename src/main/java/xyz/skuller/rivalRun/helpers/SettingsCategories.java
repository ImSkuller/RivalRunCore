package xyz.skuller.rivalRun.helpers;

import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

import static xyz.skuller.rivalRun.helpers.SettingEntry.cycle;
import static xyz.skuller.rivalRun.helpers.SettingEntry.number;
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
                number(Material.CLOCK, "Grace Period Length", "Seconds, minimum 60", "grace.gracePeriod")
        );
    }

    public static List<SettingEntry> manhunt() {
        return List.of(
                cycle(Material.PLAYER_HEAD, "Speedrunner Teams", "1 = Speedrunners vs Hunters, 2 = also vs each other", "manhunt.speedrunnerTeams", 0,
                        "1", "2"),
                number(Material.BOW, "Headstart Seconds", "How long Hunters are frozen at the start", "manhunt.headstartSeconds")
        );
    }

    public static List<SettingEntry> teams() {
        return List.of(
                number(Material.PLAYER_HEAD, "Team Count", "2 to 7 - applies on next reset", "teams.list"),
                number(Material.ARMOR_STAND, "Max Team Size", "2 to 6 - applies on next reset", "teams.max"),
                toggle(Material.DIAMOND_SWORD, "Friendly Fire", "Let teammates damage each other", "teams.friendlyFire"),
                toggle(Material.ENDER_PEARL, "Team Switching", "Allow switching teams mid-game", "teams.teamSwitching"),
                toggle(Material.CHEST, "Login GUI", "Open team select automatically on join", "teams.loginGUI"),
                cycle(Material.NAME_TAG, "Team Name Preset", "Applies on next reset", "teams.teamName", 0,
                        "Default (colors)", "Passive Mobs", "Hostile Mobs", "Flowers", "Blocks")
        );
    }

    public static List<SettingEntry> scoreboard() {
        return List.of(
                toggle(Material.OAK_SIGN, "Scoreboard", "Show the sidebar scoreboard", "scoreboard.enabled"),
                text(Material.PAPER, "Classic Title", "MiniMessage title text", "scoreboard.classic.title"),
                text(Material.PAPER, "Manhunt Title", "MiniMessage title text", "scoreboard.manhunt.title")
        );
    }

    public static List<SettingEntry> tablist() {
        return List.of(
                toggle(Material.BOOK, "Tab List", "Show the custom tab list header/footer", "tablist.enabled"),
                text(Material.WRITABLE_BOOK, "Classic Header", "Placeholders: {state} {timer} {online} {max}", "tablist.classic.header"),
                text(Material.WRITABLE_BOOK, "Classic Footer", "Placeholders: {state} {timer} {online} {max}", "tablist.classic.footer"),
                text(Material.WRITABLE_BOOK, "Manhunt Header", "Placeholders: {state} {timer} {online} {max} {speedrunners_alive} {hunters_alive}", "tablist.manhunt.header"),
                text(Material.WRITABLE_BOOK, "Manhunt Footer", "Placeholders: {state} {timer} {online} {max} {speedrunners_alive} {hunters_alive}", "tablist.manhunt.footer")
        );
    }

    public static List<SettingEntry> motd() {
        return List.of(
                toggle(Material.KNOWLEDGE_BOOK, "Dynamic MOTD", "Reflect game state in the server list", "motd.enabled"),
                text(Material.PAPER, "Classic - Waiting", "Placeholders: {online} {max} {timer}", "motd.classic.waiting"),
                text(Material.PAPER, "Classic - Starting", "Placeholders: {online} {max} {timer}", "motd.classic.starting"),
                text(Material.PAPER, "Classic - Running", "Placeholders: {online} {max} {timer}", "motd.classic.running"),
                text(Material.PAPER, "Classic - Post-game", "Placeholders: {online} {max} {timer}", "motd.classic.post"),
                text(Material.PAPER, "Manhunt - Waiting", "Placeholders: {online} {max} {timer} {speedrunners_alive} {hunters_alive}", "motd.manhunt.waiting"),
                text(Material.PAPER, "Manhunt - Starting", "Placeholders: {online} {max} {timer} {speedrunners_alive} {hunters_alive}", "motd.manhunt.starting"),
                text(Material.PAPER, "Manhunt - Running", "Placeholders: {online} {max} {timer} {speedrunners_alive} {hunters_alive}", "motd.manhunt.running"),
                text(Material.PAPER, "Manhunt - Post-game", "Placeholders: {online} {max} {timer} {speedrunners_alive} {hunters_alive}", "motd.manhunt.post")
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

    public static List<SettingEntry> playerBuffs(UUID uuid) {
        String base = "buffs." + uuid;

        return List.of(
                number(Material.APPLE, "Max Health", "-10 to +10 hearts (2 HP each), 0 = normal", base + ".health"),
                cycle(Material.COOKED_BEEF, "Saturation Drain", "How fast hunger drains", base + ".saturationRate", 2,
                        "Much Slower", "Slower", "Normal", "Faster", "Much Faster"),
                cycle(Material.SUGAR, "Movement Speed", "Passive speed/slowness", base + ".speed", 2,
                        "Slowness II", "Slowness I", "Normal", "Speed I", "Speed II"),
                cycle(Material.IRON_SWORD, "Damage Dealt", "Outgoing PvP damage multiplier", base + ".damageDealt", 2,
                        "75%", "90%", "100%", "110%", "125%"),
                cycle(Material.SHIELD, "Damage Taken", "Incoming PvP damage multiplier", base + ".damageTaken", 2,
                        "75%", "90%", "100%", "110%", "125%"),
                number(Material.GOLDEN_CARROT, "Kill Regen Duration", "Seconds of Regeneration after a player kill, 0 = off", base + ".killRegenSeconds"),
                number(Material.BLAZE_POWDER, "Kill Strength Duration", "Seconds of Strength after a player kill, 0 = off", base + ".killStrengthSeconds")
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
