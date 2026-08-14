package xyz.skuller.rivalRun.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

import java.util.ArrayList;

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

        for (Player player : Bukkit.getOnlinePlayers()) {
            resetPlayerState(player);
        }
    }

    // Wipes everything a run could have left on a player - inventory,
    // health/hunger, potion effects, XP - back to a fresh-join state.
    private void resetPlayerState(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);

        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);

        var maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) player.setHealth(maxHealth.getValue());
        player.setFoodLevel(20);
        player.setSaturation(5f);
        player.setExhaustion(0f);
        player.setFireTicks(0);
        player.setFallDistance(0f);
        player.setExp(0f);
        player.setLevel(0);
        player.setTotalExperience(0);
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
