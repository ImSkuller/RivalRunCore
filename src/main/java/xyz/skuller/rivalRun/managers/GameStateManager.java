package xyz.skuller.rivalRun.managers;

import org.bukkit.Bukkit;

public class GameStateManager {

    public enum GameStates{
        WAITING,
        STARTING,
        PAUSED,
        RUNNING,
        POST
    }

    private GameStates currentState;

    public GameStateManager() {
        this.currentState = GameStates.WAITING;
    }

    public GameStates getState() {
        return currentState;
    }

    public void setState(GameStates newState) {
        this.currentState = newState;
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Game state updated to " + newState.name());
    }

    public boolean isState(GameStates state) {
        return currentState == state;
    }

}
