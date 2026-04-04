package xyz.skuller.rivalRun;

import xyz.skuller.rivalRun.managers.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.skuller.rivalRun.managers.GameStateManager;

public final class RivalRun extends JavaPlugin {

    private static RivalRun instance;
    private static GameStateManager gameStateManager;


    @Override
    public void onEnable() {
        instance = this;
        gameStateManager = new GameStateManager();

        // Event manager
        EventManager eventManager = new EventManager();
        eventManager.RegisterEvents(this);
        eventManager.RegisterCommands(this);
        eventManager.StartConsoleEvent(getPluginMeta().getVersion());



        // Loaded Message
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Rival Run Core has been loaded successfully.");

    }

    @Override
    public void onDisable() {

        // Disabled Message
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Rival Run Core has shut down successfully.");

    }

    public static RivalRun getInstance() { return instance; }

    public GameStateManager getGameStateManager() { return gameStateManager; }
}