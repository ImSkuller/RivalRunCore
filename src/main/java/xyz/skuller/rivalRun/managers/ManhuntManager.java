package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.helpers.WinReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Owns Manhunt-only per-game state: which Speedrunner each Hunter is
// tracking (kept live with a plain Player#setCompassTarget refresh - a
// normal vanilla compass, no resource pack), and which Speedrunners have
// already used their one revival this game.
public class ManhuntManager {

    private final NamespacedKey trackerKey;

    private final Map<UUID, UUID> targets = new HashMap<>();
    private final Set<UUID> revivedOnce = new HashSet<>();

    private BukkitTask task;

    public ManhuntManager() {
        this.trackerKey = new NamespacedKey(RivalRun.getInstance(), "manhunt_tracker");
    }

    public void start(RivalRun plugin) {
        stop();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        RivalRun plugin = RivalRun.getInstance();
        if (!plugin.getGamemodeManager().isManhunt()) return;
        if (!plugin.getGameStateManager().isState(GameStateManager.GameStates.RUNNING)) return;

        for (Map.Entry<UUID, UUID> entry : targets.entrySet()) {
            Player hunter = Bukkit.getPlayer(entry.getKey());
            if (hunter == null) continue;

            Player target = Bukkit.getPlayer(entry.getValue());
            if (target == null || plugin.getSpectatorManager().isSpectating(target)) {
                autoAssignTarget(hunter);
                continue;
            }

            hunter.setCompassTarget(target.getLocation());
        }
    }

    // Gives (or refreshes) a Hunter's tracking compass and picks an initial
    // target if they don't already have one.
    public void giveTrackerCompass(Player hunter) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.displayName(Component.text("Tracking Compass", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Right-click to choose who to track", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        meta.getPersistentDataContainer().set(trackerKey, PersistentDataType.BOOLEAN, true);
        compass.setItemMeta(meta);
        hunter.getInventory().setItem(0, compass);

        if (!targets.containsKey(hunter.getUniqueId())) {
            autoAssignTarget(hunter);
        }
    }

    public boolean isTrackerCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) return false;
        Boolean flagged = item.getItemMeta().getPersistentDataContainer().get(trackerKey, PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(flagged);
    }

    public void setTarget(Player hunter, Player target) {
        targets.put(hunter.getUniqueId(), target.getUniqueId());
        hunter.setCompassTarget(target.getLocation());
    }

    // Picks the first alive Speedrunner found across every Speedrunner team.
    // Clears the hunter's target entirely if none are left alive.
    public void autoAssignTarget(Player hunter) {
        RivalRun plugin = RivalRun.getInstance();

        for (Teams team : plugin.getTeamManager().getSpeedrunnerTeams()) {
            for (UUID uuid : team.getPlayers()) {
                Player candidate = Bukkit.getPlayer(uuid);
                if (candidate != null && !plugin.getSpectatorManager().isSpectating(candidate)) {
                    setTarget(hunter, candidate);
                    return;
                }
            }
        }

        targets.remove(hunter.getUniqueId());
    }

    // Name of who a Hunter is currently tracking, or "None" - used by the
    // scoreboard.
    public String getTargetName(Player hunter) {
        UUID targetId = targets.get(hunter.getUniqueId());
        if (targetId == null) return "None";
        Player target = Bukkit.getPlayer(targetId);
        return target != null ? target.getName() : "None";
    }

    public boolean hasBeenRevived(Player player) {
        return revivedOnce.contains(player.getUniqueId());
    }

    public void markRevived(Player player) {
        revivedOnce.add(player.getUniqueId());
    }

    // Shared "come back weaker" state for both the team-wipe-to-Hunters
    // conversion and a teammate revival.
    public void applyHalfState(Player player) {
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.max(1.0, maxHealth / 2.0));
        player.setFoodLevel(10);
        player.setSaturation(2.5f);
    }

    // Called once a Speedrunner team's last alive member has just gone to
    // spectator - moves every member of that team onto the Hunters team at
    // half health/saturation, then checks whether that was the last
    // Speedrunner team left (Hunters win if so).
    public void convertTeamToHunters(Teams eliminatedTeam) {
        RivalRun plugin = RivalRun.getInstance();
        Teams hunters = plugin.getTeamManager().getHunterTeam();
        if (hunters == null) return;

        List<UUID> members = new ArrayList<>(eliminatedTeam.getPlayers());
        for (UUID uuid : members) {
            Player member = Bukkit.getPlayer(uuid);
            if (member == null) continue;

            plugin.getSpectatorManager().clearSpectator(member);
            plugin.getTeamManager().forceAssign(member, hunters);
            applyHalfState(member);
            giveTrackerCompass(member);
            member.sendRichMessage("<red>Your team was hunted down - you've joined the Hunters!");
        }

        Bukkit.broadcast(Component.text(eliminatedTeam.getName() + " has been hunted down and joins the Hunters!", NamedTextColor.RED));

        boolean anySpeedrunnersLeft = plugin.getTeamManager().getSpeedrunnerTeams().stream()
                .anyMatch(t -> t.getSize() > 0);

        if (!anySpeedrunnersLeft) {
            plugin.getWinManager().announceWin(hunters, WinReason.HUNTERS_CAUGHT_ALL);
        }
    }

    public void reset() {
        targets.clear();
        revivedOnce.clear();
    }

}
