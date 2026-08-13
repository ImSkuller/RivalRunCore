package xyz.skuller.rivalRun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.menus.AdminMenu;
import xyz.skuller.rivalRun.menus.SpectatorMenu;

import java.util.List;

public class MainCommandClass implements TabExecutor {

    private final GameCommands gameCommands;
    private final TeamCommands teamCommands;

    public MainCommandClass(GameCommands gameCommands, TeamCommands teamCommands) {
        this.gameCommands = gameCommands;
        this.teamCommands = teamCommands;
    }

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
            "spectate"
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

            sender.sendRichMessage("<red>Usage: /rr <subcommand>");
            return true;
        }

        if (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("startgame")) {
            if (!sender.hasPermission("rivalrun.game.start")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            gameCommands.startGame();
        }

        else if (args[0].equalsIgnoreCase("pause") || args[0].equalsIgnoreCase("pausegame")) {
            if (!isEnabled(sender, "commands.pauseGame")) return true;
            if (!sender.hasPermission("rivalrun.game.pause")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            gameCommands.pauseGame(sender);
        }

        else if (args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("endgame")) {
            if (!isEnabled(sender, "commands.endGame")) return true;
            if (!sender.hasPermission("rivalrun.game.end")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            gameCommands.endGame();
        }

        else if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("resetgame")) {
            if (!sender.hasPermission("rivalrun.game.reset")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            gameCommands.resetGame();
            sender.sendRichMessage("<green>The game has been reset.");
        }

        else if (args[0].equalsIgnoreCase("resume") || args[0].equalsIgnoreCase("resumegame")) {
            if (!isEnabled(sender, "commands.pauseGame")) return true;
            if (!sender.hasPermission("rivalrun.game.resume")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            gameCommands.resumeGame();
        }

        else if (args[0].equalsIgnoreCase("select") ||
                args[0].equalsIgnoreCase("selectteam") ||
                args[0].equalsIgnoreCase("teamselect"))
        {
            if (!isEnabled(sender, "commands.teamSelect")) return true;
            if (!(sender instanceof final Player player)) {
                sender.sendRichMessage("<red>Only players can use this command.");
                return true;
            }
            if (!sender.hasPermission("rivalrun.teams.select")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            teamCommands.selectTeam(player);
        }

        else if (args[0].equalsIgnoreCase("leave") ||
                args[0].equalsIgnoreCase("leaveteam") ||
                args[0].equalsIgnoreCase("teamleave"))
        {
            if (!isEnabled(sender, "commands.teamLeave")) return true;
            if (!(sender instanceof final Player player)) {
                sender.sendRichMessage("<red>Only players can use this command.");
                return true;
            }
            if (!sender.hasPermission("rivalrun.teams.leave")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            teamCommands.leaveTeam(player);
        }

        else if (args[0].equalsIgnoreCase("switch") ||
                args[0].equalsIgnoreCase("switchteam") ||
                args[0].equalsIgnoreCase("teamswitch"))
        {
            if (!isEnabled(sender, "commands.teamSelect")) return true;
            if (!(sender instanceof final Player player)) {
                sender.sendRichMessage("<red>Only players can use this command.");
                return true;
            }
            if (!sender.hasPermission("rivalrun.teams.switch")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            teamCommands.switchTeams(player);
        }

        else if (args[0].equalsIgnoreCase("lock") ||
                args[0].equalsIgnoreCase("lockteams") ||
                args[0].equalsIgnoreCase("teamslock"))
        {
            if (!(sender instanceof final Player player)) {
                sender.sendRichMessage("<red>Only players can use this command.");
                return true;
            }
            if (!sender.hasPermission("rivalrun.teams.lock")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            teamCommands.lockTeams(player);
        }

        else if (args[0].equalsIgnoreCase("unlock") ||
                args[0].equalsIgnoreCase("unlockteams") ||
                args[0].equalsIgnoreCase("teamsunlock"))
        {
            if (!(sender instanceof final Player player)) {
                sender.sendRichMessage("<red>Only players can use this command.");
                return true;
            }
            if (!sender.hasPermission("rivalrun.teams.select")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            teamCommands.unlockTeams(player);
        }

        else if (args[0].equalsIgnoreCase("spectate")) {
            if (!(sender instanceof final Player player)) {
                sender.sendRichMessage("<red>Only players can use this command.");
                return true;
            }
            if (!sender.hasPermission("rivalrun.spectate")) {
                sender.sendRichMessage("<red>You do not have the permissions required to run this command.");
                return true;
            }
            if (!RivalRun.getInstance().getSpectatorManager().isSpectating(player)) {
                sender.sendRichMessage("<red>You are not spectating right now.");
                return true;
            }
            new SpectatorMenu().open(player);
        }

        else {
            sender.sendRichMessage("<red>That is not a valid command.");
        }

        return false;
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

            return subCommands.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .toList();
        }

        return List.of();
    }
}
