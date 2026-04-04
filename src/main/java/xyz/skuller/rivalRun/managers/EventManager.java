package xyz.skuller.rivalRun.managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import xyz.skuller.rivalRun.events.JoinEvent;
import xyz.skuller.rivalRun.events.LeaveEvent;

public class EventManager {

    // Registering Events
    public void RegisterEvents(Plugin plugin) {

        plugin.getServer().getPluginManager().registerEvents(new JoinEvent(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new LeaveEvent(), plugin);

        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Registered all the events.");
    }


    // Registering Commands
    public void RegisterCommands(Plugin plugin) {


        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Registered all the commands.");
    }

    // Startup Logo Sender
    public void StartConsoleEvent(String version) {
        Bukkit.getConsoleSender().sendRichMessage(" ");
        Bukkit.getConsoleSender().sendRichMessage("<aqua>  _____   _____ ");
        Bukkit.getConsoleSender().sendRichMessage("<aqua> |  __ \\ |  __ \\    ");
        Bukkit.getConsoleSender().sendRichMessage("<aqua> | |__) || |__) |    <green>Rival Run Core <aqua>v" + version);
        Bukkit.getConsoleSender().sendRichMessage("<aqua> |  __ / |  __ /     <gold>Author: <aqua>Skuller");
        Bukkit.getConsoleSender().sendRichMessage("<aqua> | | \\ \\ | | \\ \\     <blue>Discord: https://discord.gg/WwVmxeXEgH");
        Bukkit.getConsoleSender().sendRichMessage("<aqua> |_|  \\_\\|_|  \\_\\ ");
        Bukkit.getConsoleSender().sendRichMessage(" ");
    }

}