package xyz.skuller.rivalRun.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Gamemode;
import xyz.skuller.rivalRun.helpers.GuideBook;
import xyz.skuller.rivalRun.helpers.Messages;
import xyz.skuller.rivalRun.menus.AdminMenu;
import xyz.skuller.rivalRun.menus.BuffPlayerListMenu;
import xyz.skuller.rivalRun.menus.MatchSummaryMenu;
import xyz.skuller.rivalRun.menus.SettingsMenu;
import xyz.skuller.rivalRun.menus.SpectatorMenu;
import xyz.skuller.rivalRun.menus.WorldResetConfirmMenu;

import java.util.Arrays;
import java.util.List;

// Every action is reachable two ways: the original flat command (e.g.
// "/rr switchteam", kept for backward compatibility) and a category-based
// grouped command ("/rr team switch"). Both paths call the same handler
// methods below, so there's exactly one implementation of each action.
public class MainCommandClass implements TabExecutor {

    private final GameCommands gameCommands;
    private final TeamCommands teamCommands;

    public MainCommandClass(GameCommands gameCommands, TeamCommands teamCommands) {
        this.gameCommands = gameCommands;
        this.teamCommands = teamCommands;
    }

    private static final List<String> CATEGORIES = List.of("game", "team", "admin");
    private static final List<String> GAME_ACTIONS = List.of("start", "pause", "resume", "end", "reset");
    private static final List<String> TEAM_ACTIONS = List.of("select", "leave", "switch", "lock", "unlock", "revive");
    private static final List<String> ADMIN_ACTIONS = List.of("spectator", "buffs", "resetworld", "gamemode", "settings");

    private final List<String> subCommands = List.of(
            "start", "startgame",
            "pause", "pausegame",
            "end", "endgame",
            "reset", "resetgame",
            "resume", "resumegame",
            "select", "selectteam", "teamselect",
            "leave", "leaveteam", "teamleave",
            "switch", "switchteam", "teamswitch",
            "lock", "lockteams", "teamslock",
            "unlock", "unlockteams", "teamsunlock",
            "revive",
            "spectate", "spectator",
            "resetworld", "worldreset", "newworld",
            "buffs", "buffdebuff", "playersettings", "handicaps",
            "gamemode",
            "help", "guide",
            "summary", "matchsummary", "recap"
    );

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String s,
                             @NotNull String @NotNull [] args)
    {
        if (args.length == 0) {
            if (sender instanceof final Player player
                    && sender.hasPermission("rivalrun.admin")
                    && RivalRun.getInstance().getConfig().getBoolean("gui.adminMenu", true))
            {
                new AdminMenu().open(player);
                return true;
            }

            sender.sendRichMessage("<red>Usage: /rr <subcommand>. Run <white>/rr help<red> for a guide.");
            return true;
        }

        // Grouped syntax: /rr <game|team|admin> <action> [...]
        if (CATEGORIES.contains(args[0].toLowerCase())) {
            if (args.length < 2) {
                sender.sendRichMessage("<red>Usage: /rr " + args[0].toLowerCase() + " <action>");
                return true;
            }
            dispatch(sender, args[0].toLowerCase(), args[1].toLowerCase(), rest(args, 2));
            return true;
        }

        String action = args[0].toLowerCase();
        String[] rest = rest(args, 1);

        if (action.equals("start") || action.equals("startgame")) {
            handleGameStart(sender);
        }
        else if (action.equals("pause") || action.equals("pausegame")) {
            handleGamePause(sender);
        }
        else if (action.equals("end") || action.equals("endgame")) {
            handleGameEnd(sender);
        }
        else if (action.equals("reset") || action.equals("resetgame")) {
            handleGameReset(sender);
        }
        else if (action.equals("resume") || action.equals("resumegame")) {
            handleGameResume(sender);
        }
        else if (action.equals("select") || action.equals("selectteam") || action.equals("teamselect")) {
            handleTeamSelect(sender);
        }
        else if (action.equals("leave") || action.equals("leaveteam") || action.equals("teamleave")) {
            handleTeamLeave(sender);
        }
        else if (action.equals("switch") || action.equals("switchteam") || action.equals("teamswitch")) {
            handleTeamSwitch(sender);
        }
        else if (action.equals("lock") || action.equals("lockteams") || action.equals("teamslock")) {
            handleTeamLock(sender);
        }
        else if (action.equals("unlock") || action.equals("unlockteams") || action.equals("teamsunlock")) {
            handleTeamUnlock(sender);
        }
        else if (action.equals("revive")) {
            handleTeamRevive(sender, rest);
        }
        else if (action.equals("spectate")) {
            handleSpectate(sender);
        }
        else if (action.equals("spectator")) {
            handleAdminSpectator(sender, rest);
        }
        else if (action.equals("resetworld") || action.equals("worldreset") || action.equals("newworld")) {
            handleAdminResetworld(sender);
        }
        else if (action.equals("buffs") || action.equals("buffdebuff") || action.equals("playersettings") || action.equals("handicaps")) {
            handleAdminBuffs(sender);
        }
        else if (action.equals("gamemode")) {
            handleAdminGamemode(sender, rest);
        }
        else if (action.equals("help") || action.equals("guide")) {
            handleHelp(sender, args);
        }
        else if (action.equals("summary") || action.equals("matchsummary") || action.equals("recap")) {
            handleSummary(sender);
        }
        else {
            sender.sendRichMessage("<red>That is not a valid command.");
        }

        return true;
    }

    private String[] rest(String[] args, int from) {
        return args.length > from ? Arrays.copyOfRange(args, from, args.length) : new String[0];
    }

    private void dispatch(CommandSender sender, String category, String action, String[] rest) {
        switch (category) {
            case "game" -> {
                switch (action) {
                    case "start" -> handleGameStart(sender);
                    case "pause" -> handleGamePause(sender);
                    case "resume" -> handleGameResume(sender);
                    case "end" -> handleGameEnd(sender);
                    case "reset" -> handleGameReset(sender);
                    default -> sender.sendRichMessage("<red>Usage: /rr game <" + String.join("|", GAME_ACTIONS) + ">");
                }
            }
            case "team" -> {
                switch (action) {
                    case "select" -> handleTeamSelect(sender);
                    case "leave" -> handleTeamLeave(sender);
                    case "switch" -> handleTeamSwitch(sender);
                    case "lock" -> handleTeamLock(sender);
                    case "unlock" -> handleTeamUnlock(sender);
                    case "revive" -> handleTeamRevive(sender, rest);
                    default -> sender.sendRichMessage("<red>Usage: /rr team <" + String.join("|", TEAM_ACTIONS) + ">");
                }
            }
            case "admin" -> {
                switch (action) {
                    case "spectator" -> handleAdminSpectator(sender, rest);
                    case "buffs" -> handleAdminBuffs(sender);
                    case "resetworld" -> handleAdminResetworld(sender);
                    case "gamemode" -> handleAdminGamemode(sender, rest);
                    case "settings" -> handleAdminSettings(sender);
                    default -> sender.sendRichMessage("<red>Usage: /rr admin <" + String.join("|", ADMIN_ACTIONS) + ">");
                }
            }
        }
    }

    // ---- game ----

    private void handleGameStart(CommandSender sender) {
        if (!sender.hasPermission("rivalrun.game.start")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        gameCommands.startGame();
    }

    private void handleGamePause(CommandSender sender) {
        if (!isEnabled(sender, "commands.pauseGame")) return;
        if (!sender.hasPermission("rivalrun.game.pause")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        gameCommands.pauseGame(sender);
    }

    private void handleGameResume(CommandSender sender) {
        if (!isEnabled(sender, "commands.pauseGame")) return;
        if (!sender.hasPermission("rivalrun.game.resume")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        gameCommands.resumeGame();
    }

    private void handleGameEnd(CommandSender sender) {
        if (!isEnabled(sender, "commands.endGame")) return;
        if (!sender.hasPermission("rivalrun.game.end")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        gameCommands.endGame();
    }

    private void handleGameReset(CommandSender sender) {
        if (!sender.hasPermission("rivalrun.game.reset")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        gameCommands.resetGame();
        sender.sendRichMessage("<green>The game has been reset.");
    }

    // ---- team ----

    private void handleTeamSelect(CommandSender sender) {
        if (!isEnabled(sender, "commands.teamSelect")) return;
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.teams.select")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        teamCommands.selectTeam(player);
    }

    private void handleTeamLeave(CommandSender sender) {
        if (!isEnabled(sender, "commands.teamLeave")) return;
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.teams.leave")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        teamCommands.leaveTeam(player);
    }

    private void handleTeamSwitch(CommandSender sender) {
        if (!isEnabled(sender, "commands.teamSelect")) return;
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.teams.switch")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        teamCommands.switchTeams(player);
    }

    private void handleTeamLock(CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.teams.lock")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        teamCommands.lockTeams(player);
    }

    private void handleTeamUnlock(CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.teams.select")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        teamCommands.unlockTeams(player);
    }

    private void handleTeamRevive(CommandSender sender, String[] rest) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.teams.revive")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        if (rest.length < 1) {
            sender.sendRichMessage("<red>Usage: /rr team revive <player>");
            return;
        }
        teamCommands.reviveTeammate(player, rest[0]);
    }

    // ---- spectate / admin ----

    private void handleSpectate(CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.spectate")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        if (!RivalRun.getInstance().getSpectatorManager().isSpectating(player)) {
            sender.sendRichMessage("<red>You are not spectating right now.");
            return;
        }
        new SpectatorMenu().open(player);
    }

    private void handleAdminSpectator(CommandSender sender, String[] rest) {
        if (!sender.hasPermission("rivalrun.game.spectator")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        if (rest.length < 1) {
            sender.sendRichMessage("<red>Usage: /rr admin spectator <player>");
            return;
        }

        Player target = Bukkit.getPlayer(rest[0]);
        if (target == null) {
            sender.sendRichMessage("<red>That player is not online.");
            return;
        }

        boolean nowSpectating = RivalRun.getInstance().getSpectatorManager().toggleSpectator(target);
        if (nowSpectating) {
            sender.sendRichMessage("<green>" + target.getName() + " is now spectating.");
            target.sendMessage(Messages.get("messages.spectatorEnabled"));
        } else {
            sender.sendRichMessage("<green>" + target.getName() + " is no longer spectating.");
            target.sendMessage(Messages.get("messages.spectatorDisabled"));
        }
    }

    private void handleAdminResetworld(CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.game.resetworld")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        new WorldResetConfirmMenu().open(player);
    }

    private void handleAdminBuffs(CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.game.buffs")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        new BuffPlayerListMenu().open(player);
    }

    private void handleAdminGamemode(CommandSender sender, String[] rest) {
        if (!sender.hasPermission("rivalrun.game.gamemode")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        if (rest.length < 1) {
            sender.sendRichMessage("<red>Usage: /rr admin gamemode <classic|manhunt>");
            return;
        }

        Gamemode mode;
        try {
            mode = Gamemode.valueOf(rest[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendRichMessage("<red>Unknown gamemode. Use classic or manhunt.");
            return;
        }

        RivalRun.getInstance().getGamemodeManager().switchTo(mode, sender);
    }

    private void handleAdminSettings(CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("rivalrun.admin")) {
            sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
            return;
        }
        new SettingsMenu().open(player);
    }

    // ---- misc ----

    private void handleHelp(CommandSender sender, String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }

        boolean wantsPlayerView = args.length >= 2 && args[1].equalsIgnoreCase("player");
        if (sender.hasPermission("rivalrun.admin") && !wantsPlayerView) {
            GuideBook.openAdminGuide(player);
        } else {
            GuideBook.openPlayerGuide(player);
        }
    }

    private void handleSummary(CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Only players can use this command.");
            return;
        }
        new MatchSummaryMenu().open(player);
    }

    private boolean isEnabled(CommandSender sender, String configPath) {
        if (RivalRun.getInstance().getConfig().getBoolean(configPath, true)) return true;
        sender.sendRichMessage("<red>That command is disabled on this server.");
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String @NonNull [] args)
    {
        if (args.length == 1) {
            String input = args[0].toLowerCase();

            return java.util.stream.Stream.concat(subCommands.stream(), CATEGORIES.stream())
                    .filter(cmd -> cmd.startsWith(input))
                    .toList();
        }

        if (args.length == 2) {
            String category = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            List<String> actions = switch (category) {
                case "game" -> GAME_ACTIONS;
                case "team" -> TEAM_ACTIONS;
                case "admin" -> ADMIN_ACTIONS;
                default -> List.of();
            };

            if (!actions.isEmpty()) {
                return actions.stream().filter(a -> a.startsWith(input)).toList();
            }

            if (category.equals("spectator")) {
                return onlinePlayerNames(input);
            }
        }

        if (args.length == 3) {
            String category = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            String input = args[2].toLowerCase();

            if (category.equals("admin") && action.equals("spectator")) {
                return onlinePlayerNames(input);
            }
            if (category.equals("team") && action.equals("revive")) {
                return onlinePlayerNames(input);
            }
            if (category.equals("admin") && action.equals("gamemode")) {
                return List.of("classic", "manhunt").stream().filter(m -> m.startsWith(input)).toList();
            }
        }

        return List.of();
    }

    private List<String> onlinePlayerNames(String input) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input))
                .toList();
    }
}
