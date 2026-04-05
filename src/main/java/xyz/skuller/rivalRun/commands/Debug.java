package xyz.skuller.rivalRun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.managers.GameStateManager;

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

        GameStateManager gameStateManager = RivalRun.getInstance().getGameStateManager();

        if (args[0].equalsIgnoreCase("MODE")) {

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

        return true;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String s,
                                                @NotNull String @NotNull [] args)
    {
        if (args.length == 1) {
            return List.of("mode");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("mode")) {
            return java.util.Arrays.stream(GameStateManager.GameStates.values())
                    .map(Enum::name)
                    .toList();
        }

        return List.of();
    }
}
