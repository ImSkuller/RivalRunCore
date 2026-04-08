package xyz.skuller.rivalRun;

import xyz.skuller.rivalRun.commands.GameCommands;
import xyz.skuller.rivalRun.commands.TeamCommands;
import xyz.skuller.rivalRun.managers.ConfigManager;
import xyz.skuller.rivalRun.managers.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

public final class RivalRun extends JavaPlugin {

    private static RivalRun instance;
    private static GameStateManager gameStateManager;
    private static TeamsManager teamManager;
    private static GameCommands gc;
    private static TeamCommands tc;
    double CURRENT_VERSION = 0.1;


    @Override
    public void onEnable() {
        instance = this;
        gameStateManager = new GameStateManager();
        teamManager = new TeamsManager(this);
        gc = new GameCommands();
        tc = new TeamCommands();

        // Event manager
        EventManager eventManager = new EventManager();
        eventManager.RegisterEvents(this);
        eventManager.RegisterCommands(this);
        eventManager.StartConsoleEvent(getPluginMeta().getVersion());

        // Loading Configuration File
        new ConfigManager().ConfigUpdate(CURRENT_VERSION, this);

        // Team Manager Things.
        teamManager.loadTeamsFromConfig();


        // Loaded Message
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Rival Run Core has been loaded successfully.");

    }

    @Override
    public void onDisable() {

        // Disabled Message
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Rival Run Core has shut down successfully.");

    }

    public static RivalRun getInstance() {return instance;}

    public GameStateManager getGameStateManager() {return gameStateManager;}

    public TeamsManager getTeamManager() {return teamManager;}

    public GameCommands getGameCommands() {return gc;}

    public TeamCommands getTeamCommands() {return tc;}
}