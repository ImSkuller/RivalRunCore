package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.AchievementType;
import xyz.skuller.rivalRun.helpers.Teams;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Tracks the six team-speedrunning route milestones (see AchievementType).
// Each is claimed once, globally, by whichever team gets there first.
public class AchievementManager {

    private final Map<AchievementType, Teams> claimed = new EnumMap<>(AchievementType.class);

    public void award(Teams team, AchievementType type) {
        RivalRun plugin = RivalRun.getInstance();

        if (!plugin.getConfig().getBoolean("achievements.enabled", true)) return;
        if (!plugin.getGameStateManager().isState(GameStateManager.GameStates.RUNNING)) return;
        if (claimed.containsKey(type)) return;

        claimed.put(type, team);

        Bukkit.broadcast(Component.text("⭐ ", NamedTextColor.YELLOW)
                .append(Component.text(team.getName().toUpperCase(), team.getColor(), TextDecoration.BOLD))
                .append(Component.text(" was first to reach ", NamedTextColor.GRAY))
                .append(Component.text(type.getDisplayName() + "!", NamedTextColor.WHITE)));

        for (UUID uuid : team.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.4f);
            }
        }
    }

    public boolean isClaimed(AchievementType type) {
        return claimed.containsKey(type);
    }

    public long countFor(Teams team) {
        return claimed.values().stream().filter(owner -> owner == team).count();
    }

    // Iteration order follows AchievementType's declared (route) order,
    // since claimed is an EnumMap.
    public List<AchievementType> getAchievementsFor(Teams team) {
        return claimed.entrySet().stream()
                .filter(entry -> entry.getValue() == team)
                .map(Map.Entry::getKey)
                .toList();
    }

    public void reset() {
        claimed.clear();
    }

}
