package xyz.skuller.rivalRun.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

public class GameCommands {
    GameStateManager gsm = RivalRun.getInstance().getGameStateManager();
    TeamsManager tmm = RivalRun.getInstance().getTeamManager();
    GameStateManager.GameStates prevState;

    public void startGame() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (tmm.getPlayerTeam(player) == null) {
                RivalRun.getInstance().getSpectatorManager().makeSpectator(player);
            }
        }
        RivalRun.getInstance().getBuffManager().applyAll();

        if (RivalRun.getInstance().getGamemodeManager().isManhunt()) {
            Teams hunters = tmm.getHunterTeam();
            if (hunters != null) {
                for (var uuid : hunters.getPlayers()) {
                    Player hunter = Bukkit.getPlayer(uuid);
                    if (hunter != null) {
                        RivalRun.getInstance().getManhuntManager().giveTrackerCompass(hunter);
                    }
                }
            }
        }

        gsm.startGame();
    }

    public void endGame() {
        gsm.resetGame();
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<red>Someone ended the game."));
    }

    public void resetGame() {
        gsm.resetGame();
        tmm.reset();
        RivalRun.getInstance().getManhuntManager().reset();
    }

    public void pauseGame(CommandSender player) {
        if (!(gsm.getState().equals(GameStateManager.GameStates.PAUSED))) {
            prevState = gsm.getState();
            gsm.setState(GameStateManager.GameStates.PAUSED);
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<red>The game has been paused by someone."));
        } else {
            player.sendRichMessage("<red>The game is already paused.");
        }
    }

    public void resumeGame() {
        if (prevState == null) {
            prevState = GameStateManager.GameStates.RUNNING;
        }
        gsm.setState(prevState);
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<green>The game is now unpaused."));
    }
}
