package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.skuller.rivalRun.RivalRun;

import java.time.Duration;

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

    // Run timer bookkeeping
    private long runStartMillis;
    private long runEndMillis;
    private long pausedMillis;
    private long pauseStartedMillis;

    // The grace-period countdown task, kept so it can be cancelled on reset
    // instead of running on indefinitely (e.g. if the game is reset/ended
    // while grace is still active).
    private BukkitTask graceTask;

    // Manhunt's headstart: Hunters are frozen (see GameStateEvents.onMove)
    // for this countdown while Speedrunners get a head start. Mirrors the
    // grace-period task's pause-freezing and cancel-on-reset behavior.
    private boolean headstartActive;
    private int headstartTimeLeft;
    private BukkitTask headstartTask;


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
        if (this.currentState == newState) return;

        GameStates previousState = this.currentState;
        long now = System.currentTimeMillis();

        if (previousState == GameStates.PAUSED && newState == GameStates.RUNNING && runStartMillis != 0L) {
            pausedMillis += now - pauseStartedMillis;
        }
        if (newState == GameStates.PAUSED) {
            pauseStartedMillis = now;
        }
        if (newState == GameStates.POST) {
            runEndMillis = now;
        }

        this.currentState = newState;
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Game state updated to " + newState.name());
    }


    // Starts the elapsed run timer. The countdown (STARTING) and grace period
    // both happen before this is called, so the timer stays at 0:00 through
    // both and only starts once the run is actually live.
    public void startTimer() {
        if (runStartMillis == 0L) {
            runStartMillis = System.currentTimeMillis();
        }
    }


    // Seconds elapsed since the run started, excluding any time spent paused
    public long getElapsedSeconds() {
        if (runStartMillis == 0L) return 0L;

        long end;
        if (runEndMillis != 0L) {
            end = runEndMillis;
        } else if (currentState == GameStates.PAUSED) {
            end = pauseStartedMillis;
        } else {
            end = System.currentTimeMillis();
        }

        return Math.max(0L, (end - runStartMillis - pausedMillis) / 1000L);
    }


    // Formats the elapsed run time as mm:ss, or hh:mm:ss once past an hour
    public String getFormattedElapsed() {
        long total = getElapsedSeconds();
        long hours = total / 3600;
        long minutes = (total % 3600) / 60;
        long seconds = total % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
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

        if (graceTask != null) {
            graceTask.cancel();
        }

        this.gracePeriod = true;
        this.graceTimeLeft = seconds;
        this.timeColor = NamedTextColor.GREEN;

        graceTask = new BukkitRunnable() {
            @Override
            public void run() {

                // Freeze the grace countdown while the game is paused - skip
                // this tick entirely (no decrement, no action bar/sound, and
                // crucially no falling through to the "grace ended" branch)
                // so it resumes from exactly where it left off.
                if (currentState == GameStates.PAUSED) {
                    return;
                }

                if (graceTimeLeft <= 0) {
                    gracePeriod = false;
                    startTimer();

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
                        Component.text("Grace Period: ", NamedTextColor.WHITE)
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


    // True while Manhunt's headstart countdown is still running - Hunters
    // are frozen in place (see GameStateEvents.onMove) until it ends.
    public boolean isHeadstart() {
        return currentState == GameStates.RUNNING && headstartActive;
    }

    // Manhunt headstart manager - mirrors startGracePeriod()'s cancellable
    // task, pause-freezing, and last-5-seconds alert pattern.
    public void startHeadstart(int seconds) {

        if (headstartTask != null) {
            headstartTask.cancel();
        }

        // Manhunt never calls startGracePeriod(), so without this,
        // gracePeriod stays true (its constructor/reset default) for the
        // entire match - isGracePeriod() would then report true forever,
        // and GameStateEvents.onDamage() cancels ALL player-vs-player
        // damage while it's true, silently disabling PvP for the whole game.
        this.gracePeriod = false;

        this.headstartActive = true;
        this.headstartTimeLeft = seconds;
        this.timeColor = NamedTextColor.GREEN;

        headstartTask = new BukkitRunnable() {
            @Override
            public void run() {

                if (currentState == GameStates.PAUSED) {
                    return;
                }

                if (headstartTimeLeft <= 0) {
                    headstartActive = false;
                    startTimer();

                    Bukkit.broadcast(Component.text(
                            "The headstart is over - Hunters are free to move!",
                            NamedTextColor.RED
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

                Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(
                        Component.text("Headstart: ", NamedTextColor.WHITE)
                                .append(Component.text(headstartTimeLeft, timeColor))
                                .append(Component.text("s", NamedTextColor.GRAY))
                ));

                if (headstartTimeLeft <= 5) {

                    timeColor = NamedTextColor.RED;

                    float pitch = 1.0f + (5 - headstartTimeLeft) * 0.2f;

                    Bukkit.getOnlinePlayers().forEach(player -> player.playSound(
                            player.getLocation(),
                            Sound.BLOCK_NOTE_BLOCK_HAT,
                            1f,
                            pitch
                    ));
                }

                headstartTimeLeft--;
            }
        }.runTaskTimer(RivalRun.getInstance(), 0L, 20L);
    }


    // Countdown before the game starts and the game start logic.
    public void startCountdown(int seconds) {
        boolean isGraceEnabled;
        int graceTime;

        isGraceEnabled = RivalRun.getInstance().getConfig().getBoolean("grace.enabled");
        graceTime= RivalRun.getInstance().getConfig().getInt("grace.gracePeriod");

        setState(GameStates.STARTING);

        new BukkitRunnable() {

            int timeLeft = seconds;

            @Override
            public void run() {

                if (timeLeft <= 0 ) {
                    Title goTitle = Title.title(
                            Component.text("GO!", NamedTextColor.GREEN),
                            Component.text("Good luck!", NamedTextColor.GRAY),
                            Title.Times.times(Duration.ZERO, Duration.ofMillis(2000), Duration.ofMillis(500))
                    );
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.showTitle(goTitle);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1f,1f);
                    });

                    cancel();

                    setState(GameStates.RUNNING);
                    if (RivalRun.getInstance().getGamemodeManager().isManhunt()) {
                        int headstartSeconds = RivalRun.getInstance().getConfig().getInt("manhunt.headstartSeconds", 30);
                        startHeadstart(headstartSeconds);
                    } else if (isGraceEnabled) {
                        startGracePeriod(graceTime);
                    } else {
                        startTimer();
                    }
                    Bukkit.broadcast(Component.text("The game has begun.", NamedTextColor.GREEN));

                    return;
                }

                Title countdownTitle = Title.title(
                        Component.text(timeLeft, NamedTextColor.YELLOW),
                        Component.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(1000), Duration.ZERO)
                );
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.showTitle(countdownTitle);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                });

                timeLeft--;
            }

        }.runTaskTimer(RivalRun.getInstance(), 0L, 20L);
    }



    // Function that handles all the game start logic such as grace period and game states and all that
    public void startGame() {
        boolean isTeamSwitchingEnabled = RivalRun.getInstance().getConfig().getBoolean("teams.teamSwitching");
        if (!(isTeamSwitchingEnabled)) {
            RivalRun.getInstance().getTeamManager().lockTeams();
        }
        startCountdown(5);
    }

    // Function that's used to reset the game after it ends.
    public void resetGame() {
        if (graceTask != null) {
            graceTask.cancel();
            graceTask = null;
        }
        if (headstartTask != null) {
            headstartTask.cancel();
            headstartTask = null;
        }
        gracePeriod = true;
        graceTimeLeft = 0;
        headstartActive = false;
        headstartTimeLeft = 0;
        timeColor = NamedTextColor.GREEN;
        runStartMillis = 0L;
        runEndMillis = 0L;
        pausedMillis = 0L;
        pauseStartedMillis = 0L;
        RivalRun.getInstance().getTeamManager().unlockTeams();
        RivalRun.getInstance().getBuffManager().clearAll();

        setState(GameStates.WAITING);
    }

}
