package xyz.skuller.rivalRun;

import xyz.skuller.rivalRun.commands.GameCommands;
import xyz.skuller.rivalRun.commands.TeamCommands;
import xyz.skuller.rivalRun.managers.ConfigManager;
import xyz.skuller.rivalRun.managers.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.ScoreboardManager;
import xyz.skuller.rivalRun.managers.SpectatorManager;
import xyz.skuller.rivalRun.managers.TabListManager;
import xyz.skuller.rivalRun.managers.TeamsManager;
import xyz.skuller.rivalRun.managers.WinManager;

public final class RivalRun extends JavaPlugin {

    private static RivalRun instance;
    private static GameStateManager gameStateManager;
    private static TeamsManager teamManager;
    private static SpectatorManager spectatorManager;
    private static ScoreboardManager scoreboardManager;
    private static TabListManager tabListManager;
    private static WinManager winManager;
    private static GameCommands gc;
    private static TeamCommands tc;
    double CURRENT_VERSION = 0.2;


    @Override
    public void onEnable() {
        instance = this;
        gameStateManager = new GameStateManager();
        teamManager = new TeamsManager(this);
        spectatorManager = new SpectatorManager();
        scoreboardManager = new ScoreboardManager();
        tabListManager = new TabListManager();
        winManager = new WinManager();
        gc = new GameCommands();
        tc = new TeamCommands();

        // Loading Configuration File
        new ConfigManager().update(this, CURRENT_VERSION);

        // Event manager
        EventManager eventManager = new EventManager();
        eventManager.RegisterEvents(this);
        eventManager.RegisterCommands(this);
        eventManager.StartConsoleEvent(getPluginMeta().getVersion());

        // Team Manager Things.
        teamManager.loadTeamsFromConfig();

        // Live UI (sidebar scoreboard + tab list header/footer)
        scoreboardManager.start(this);
        tabListManager.start(this);

        // Loaded Message
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Rival Run Core has been loaded successfully.");

    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) scoreboardManager.stop();
        if (tabListManager != null) tabListManager.stop();

        // Disabled Message
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Rival Run Core has shut down successfully.");

    }

    public static RivalRun getInstance() {return instance;}

    public GameStateManager getGameStateManager() {return gameStateManager;}

    public TeamsManager getTeamManager() {return teamManager;}

    public SpectatorManager getSpectatorManager() {return spectatorManager;}

    public ScoreboardManager getScoreboardManager() {return scoreboardManager;}

    public TabListManager getTabListManager() {return tabListManager;}

    public WinManager getWinManager() {return winManager;}

    public GameCommands getGameCommands() {return gc;}

    public TeamCommands getTeamCommands() {return tc;}
}
