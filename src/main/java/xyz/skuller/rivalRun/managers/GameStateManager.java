package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.skuller.rivalRun.RivalRun;

public class GameStateManager {

    public enum GameStates{
        WAITING,
        STARTING,
        PAUSED,
        RUNNING,
        POST
    }

    // All the variables that controls the game state
    private boolean gracePeriod;
    private int graceTimeLeft;
    private GameStates currentState;
    private NamedTextColor timeColor;


    // Constructor
    public GameStateManager() {
        this.currentState = GameStates.WAITING;
        this.gracePeriod = true;
    }


    // Function to get the current state of the game
    public GameStates getState() {
        return currentState;
    }


    // Function used to set the state of the game
    public void setState(GameStates newState) {
        this.currentState = newState;
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Game state updated to " + newState.name());
    }


    // Function used to check if the state of the game is the desired state
    public boolean isState(GameStates state) {
        return currentState == state;
    }


    // Checks if the grace period has ended or not
    public boolean isGracePeriod() {
        return currentState == GameStates.RUNNING && gracePeriod;
    }

    // Grace period manager
    public void startGracePeriod(int seconds) {

        this.gracePeriod = true;
        this.graceTimeLeft = seconds;
        this.timeColor = NamedTextColor.GREEN;

        new BukkitRunnable() {
            @Override
            public void run() {

                if (graceTimeLeft <= 0) {
                    gracePeriod = false;

                    Bukkit.broadcast(Component.text(
                            "Grace Period has ended, PVP is now enabled.",
                            NamedTextColor.GREEN
                    ));

                    Bukkit.getOnlinePlayers().forEach(p ->
                            p.playSound(p.getLocation(),
                                    Sound.ENTITY_ENDER_DRAGON_GROWL,
                                    1f,
                                    1f)
                    );

                    cancel();
                    return;
                }

                // Player Action Bar Update
                Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(
                        Component.text("Grace Period: ", NamedTextColor.GOLD)
                                .append(Component.text(graceTimeLeft, timeColor))
                                .append(Component.text("s", NamedTextColor.GRAY))
                ));

                // Alerts for the last 5 seconds
                if (graceTimeLeft <= 5) {

                    timeColor = NamedTextColor.RED;

                    float pitch = 1.0f + (5 - graceTimeLeft) * 0.2f;

                    Bukkit.getOnlinePlayers().forEach(player -> player.playSound(
                            player.getLocation(),
                            Sound.BLOCK_NOTE_BLOCK_HAT,
                            1f,
                            pitch
                    ));
                }

                graceTimeLeft--;
            }
        }.runTaskTimer(RivalRun.getInstance(), 0L, 20L);
    }


    // Countdown before the game starts and the game start logic.
    public void startCountdown(int seconds) {
        setState(GameStates.STARTING);

        new BukkitRunnable() {

            int timeLeft = seconds;

            @Override
            public void run() {

                if (timeLeft <= 0 ) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendTitle("§aGO!", "§7Good luck!", 0, 40, 10);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1f,1f);
                    });

                    cancel();

                    setState(GameStates.RUNNING);
                    startGracePeriod(300);
                    Bukkit.broadcast(Component.text("The game has begun.", NamedTextColor.GREEN));

                    return;
                }

                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendTitle("§e" + timeLeft, "", 0, 20, 0);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                });

                timeLeft--;
            }

        }.runTaskTimer(RivalRun.getInstance(), 0L, 20L);
    }



    // Function that handles all the game start logic such as grace period and game states and all that
    public void startGame() {
        startCountdown(5);
    }

}
