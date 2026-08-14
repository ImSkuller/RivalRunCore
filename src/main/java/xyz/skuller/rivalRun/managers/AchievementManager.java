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
import xyz.skuller.rivalRun.helpers.WinReason;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Tracks every achievement (see AchievementType, tagged per-gamemode).
// Each is claimed once, globally, by whichever team gets there first.
public class AchievementManager {

    private final Map<AchievementType, Teams> claimed = new EnumMap<>(AchievementType.class);

    // Which teams have had a member die this run - used by win-time
    // achievements like Flawless Victory / Never Say Die.
    private final Set<Teams> teamsWithDeaths = new HashSet<>();

    public void award(Teams team, AchievementType type) {
        RivalRun plugin = RivalRun.getInstance();

        if (!plugin.getConfig().getBoolean("achievements.enabled", true)) return;
        if (!plugin.getGameStateManager().isState(GameStateManager.GameStates.RUNNING)) return;
        if (type.getGamemode() != plugin.getGamemodeManager().getActive()) return;
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

    public void recordDeath(Teams team) {
        if (team != null) teamsWithDeaths.add(team);
    }

    public boolean hasTeamDied(Teams team) {
        return teamsWithDeaths.contains(team);
    }

    // Called once from WinManager.announceWin(), before the game state
    // flips to POST (award() requires RUNNING), so every win-time
    // achievement gets one single place to live.
    public void onWin(Teams team, WinReason reason) {
        RivalRun plugin = RivalRun.getInstance();
        GameStateManager gsm = plugin.getGameStateManager();
        TeamsManager tm = plugin.getTeamManager();
        SpectatorManager sm = plugin.getSpectatorManager();
        ManhuntManager mm = plugin.getManhuntManager();
        long elapsed = gsm.getElapsedSeconds();
        boolean manhunt = plugin.getGamemodeManager().isManhunt();

        if (reason == WinReason.DRAGON) {
            if (!hasTeamDied(team)) award(team, AchievementType.FLAWLESS_VICTORY);

            if (manhunt) {
                if (!hasTeamDied(team)) award(team, AchievementType.NEVER_SAY_DIE);
                if (elapsed < 900) award(team, AchievementType.SPEED_DEMON);
                if (sm.countAlive(team) == 1) award(team, AchievementType.LAST_ONE_STANDING);
                if (sm.countAlive(team) == team.getSize()) {
                    award(team, AchievementType.FULL_SQUAD);
                } else {
                    award(team, AchievementType.SACRIFICE_PLAY);
                }
                if (tm.getSpeedrunnerTeams().stream().anyMatch(t -> t != team && t.getSize() == 0)) {
                    award(team, AchievementType.UNDERDOG_VICTORY);
                }
                Teams hunters = tm.getHunterTeam();
                if (hunters != null && hunters.getSize() >= team.getSize() * 2) {
                    award(team, AchievementType.AGAINST_ALL_ODDS);
                }
                boolean anyRevivedSurvivor = team.getPlayers().stream()
                        .map(Bukkit::getPlayer)
                        .filter(java.util.Objects::nonNull)
                        .anyMatch(mm::hasBeenRevived);
                if (anyRevivedSurvivor) award(team, AchievementType.COMEBACK_KID);
            }
        }

        if (reason == WinReason.HUNTERS_CAUGHT_ALL) {
            award(team, AchievementType.THE_HUNT_IS_OVER);
            if (elapsed < 600) award(team, AchievementType.QUICK_HUNT);
            if (!isClaimed(AchievementType.ENTER_END)) award(team, AchievementType.CLEAN_SWEEP);
            if (mm.neverRevived()) award(team, AchievementType.IRON_CURTAIN);
            if (!mm.anyHunterDied()) award(team, AchievementType.PERFECT_HUNT);
        }

        if (manhunt && elapsed >= 2400) {
            award(team, AchievementType.MARATHON);
        }
    }

    public void reset() {
        claimed.clear();
        teamsWithDeaths.clear();
    }

}
