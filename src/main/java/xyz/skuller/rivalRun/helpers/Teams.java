package xyz.skuller.rivalRun.helpers;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Teams {

    private final String name;
    private final NamedTextColor color;
    private final Set<UUID> players;
    private final TeamRole role;

    public Teams(String name, NamedTextColor color) {
        this(name, color, TeamRole.NONE);
    }

    public Teams(String name, NamedTextColor color, TeamRole role) {
        this.name = name;
        this.color = color;
        this.players = new HashSet<>();
        this.role = role;
    }

    public TeamRole getRole() {
        return role;
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return players.size();
    }
}
