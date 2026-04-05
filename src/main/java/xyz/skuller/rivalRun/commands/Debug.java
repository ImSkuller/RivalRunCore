package xyz.skuller.rivalRun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.skuller.rivalRun.RivalRun;

import java.util.List;

public class Debug implements TabExecutor {

    Boolean debugEnabled = RivalRun.getInstance().getConfig().getBoolean("commands.debug");

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender,
                             @NotNull Command command,
                             @NotNull String s,
                             @NotNull String @NotNull [] strings)
    {


        if (!debugEnabled) { // Remove the exclamation mark before release
            commandSender.sendRichMessage("<red>Debug command is not enabled");
            return true;
        }

        return true;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender,
                                                @NotNull Command command,
                                                @NotNull String s,
                                                @NotNull String @NotNull [] strings)
    {
        return List.of();
    }
}
