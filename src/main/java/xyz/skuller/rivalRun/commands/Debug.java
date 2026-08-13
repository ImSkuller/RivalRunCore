package xyz.skuller.rivalRun.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.menus.TeamSelectMenu;

import java.util.Arrays;
import java.util.List;

public class Debug implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String s,
                             @NotNull String @NotNull [] args)
    {

        boolean debugEnabled = RivalRun.getInstance().getConfig().getBoolean("commands.debug");
        if (!debugEnabled) {
            sender.sendRichMessage("<red>Debug command is not enabled");
            return true;
        }

        if (!sender.isOp()) {
            sender.sendRichMessage("<red>You do not have the permission/s required to run this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendRichMessage("<red>Usage: /rrdebug <mode|game|resetConfig|teamGui|getTeams|togglefly>");
            return true;
        }

        GameStateManager gameStateManager = RivalRun.getInstance().getGameStateManager();

        if (args[0].equalsIgnoreCase("MODE")) {

            if (args.length < 2) {
                sender.sendRichMessage("<red>Usage: /rrdebug mode <state>");
                return true;
            }

            GameStateManager.GameStates newState;

            try {
                newState = GameStateManager.GameStates.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendRichMessage("<red>Invalid game state!");
                return true;
            }

            if (gameStateManager.isState(newState)) {
                sender.sendRichMessage("<red>The game state is already " + newState);
                return true;
            }

            gameStateManager.setState(newState);
            sender.sendRichMessage("<green>Game state has been set to " + newState + ", You may now test that state.");
            return true;
        }

        else if (args[0].equalsIgnoreCase("GAME")) {
            if (args.length < 2) {
                sender.sendRichMessage("<red>Usage: /rrdebug game <start|stop>");
                return true;
            }
            if (args[1].equalsIgnoreCase("start")) {
                gameStateManager.startGame();
            }
            else if (args[1].equalsIgnoreCase("stop")) {
                if (gameStateManager.isState(GameStateManager.GameStates.RUNNING) || gameStateManager.isState(GameStateManager.GameStates.PAUSED))
                {
                    sender.sendRichMessage("\n<green>The game has now ended.");
                    gameStateManager.resetGame();
                } else {
                    sender.sendRichMessage("<red>You can only use this command when the game is already running.");
                }
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("RESETCONFIG")) {
            // Deliberately destructive - this is the dev-only "wipe it and start over"
            // command. The automatic version migration in ConfigManager never does this.
            var instance = RivalRun.getInstance();

            instance.saveResource("config.yml", true);
            instance.reloadConfig();

            Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Config files reset to defaults.");
            sender.sendRichMessage("<green>Reset all the config files to their defaults.");
        }

        else if (args[0].equalsIgnoreCase("teamGui")) {
            if (!(sender instanceof final Player player)) {
                sender.sendRichMessage("<red>Only players can use this command");
                return true;
            }

            new TeamSelectMenu().open(player);
        }

        else if (args[0].equalsIgnoreCase("getTeams")) {
            sender.sendRichMessage("<green>Listed below are all the teams.");
            sender.sendRichMessage("<aqua>" + RivalRun.getInstance().getTeamManager().getFormattedTeams());
            return true;
        }

        else if (args[0].equalsIgnoreCase("togglefly")) {
            if (!(sender instanceof final Player player)) {
                sender.sendRichMessage("<red>Only players can use this command");
                return true;
            }

            player.setAllowFlight(true);
            player.setFlying(true);

        }

        return true;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String s,
                                                @NotNull String @NotNull [] args)
    {
        if (args.length == 1) {
            return List.of("Mode", "Game", "resetConfig", "teamGUI", "getTeams", "togglefly");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("mode")) {
            return Arrays.stream(GameStateManager.GameStates.values())
                    .map(Enum::name)
                    .toList();
        }

        else if (args.length == 2 && args[0].equalsIgnoreCase("game")) {
            return List.of("Start", "Stop");
        }

        return List.of();
    }
}
