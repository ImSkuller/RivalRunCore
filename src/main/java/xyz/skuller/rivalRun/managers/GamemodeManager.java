package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Gamemode;
import xyz.skuller.rivalRun.menus.TeamSelectMenu;

// Tracks which overall ruleset (Classic or Manhunt) is currently active and
// handles switching between them. Switching is only allowed while the game
// is WAITING, since Classic and Manhunt use fundamentally different team
// structures - it resets teams/state for the new mode and reopens the team
// select menu for everyone online.
public class GamemodeManager {

    private Gamemode active = Gamemode.CLASSIC;

    // Called once from onEnable() after the config has been loaded/migrated -
    // the constructor itself can't read config yet, since manager
    // instantiation happens before ConfigManager.update() (same ordering
    // TeamsManager.loadTeamsFromConfig() relies on).
    public void loadFromConfig() {
        String stored = RivalRun.getInstance().getConfig().getString("gamemode.active", "CLASSIC");
        try {
            active = Gamemode.valueOf(stored.toUpperCase());
        } catch (IllegalArgumentException e) {
            active = Gamemode.CLASSIC;
        }
    }

    public Gamemode getActive() {
        return active;
    }

    public boolean isManhunt() {
        return active == Gamemode.MANHUNT;
    }

    public boolean isClassic() {
        return active == Gamemode.CLASSIC;
    }

    // Returns true on success. Fails (with a message to the sender) if the
    // game isn't WAITING, or the mode is already active.
    public boolean switchTo(Gamemode mode, org.bukkit.command.CommandSender sender) {
        RivalRun plugin = RivalRun.getInstance();

        if (mode == active) {
            sender.sendRichMessage("<red>Rival Run is already running " + mode.name() + ".");
            return false;
        }

        if (!plugin.getGameStateManager().isState(GameStateManager.GameStates.WAITING)) {
            sender.sendRichMessage("<red>End or reset the current game before switching gamemodes.");
            return false;
        }

        this.active = mode;
        plugin.getConfig().set("gamemode.active", mode.name());
        plugin.saveConfig();

        plugin.getGameCommands().resetGame();

        Bukkit.broadcast(Component.text("Rival Run is now running ", NamedTextColor.GOLD)
                .append(Component.text(mode.name(), NamedTextColor.YELLOW))
                .append(Component.text(". Pick your team!", NamedTextColor.GOLD)));

        for (Player player : Bukkit.getOnlinePlayers()) {
            new TeamSelectMenu().open(player);
        }

        return true;
    }

}
