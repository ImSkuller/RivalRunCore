package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.TeamRole;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.helpers.WinReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

// Owns Manhunt-only per-game state: which Speedrunner each Hunter is
// tracking (kept live with a plain Player#setCompassTarget refresh - a
// normal vanilla compass, no resource pack), which Speedrunners have
// already used their one revival this game, damage dealt to the Ender
// Dragon per Speedrunner team (to credit a win fairly if a Hunter lands
// the killing blow), and which dead Speedrunners are locked to spectating
// through a living teammate.
public class ManhuntManager {

    private final NamespacedKey trackerKey;
    private final Random random = new Random();

    private final Map<UUID, UUID> targets = new HashMap<>();
    private final Set<UUID> revivedOnce = new HashSet<>();
    private final Map<Teams, Double> dragonDamageByTeam = new HashMap<>();

    // Set in onDeath (which knows the killer) and consumed in onRespawn
    // (which is where the actual spectator conversion happens) - see
    // isPermanentDeath(). PlayerRespawnEvent has no death-cause info of its
    // own, so this is how the decision crosses from one event to the other.
    private final Set<UUID> permanentDeathPending = new HashSet<>();

    // Dead Speedrunner -> the living teammate they're locked to spectating
    // through (see ManhuntListener's movement/interaction restrictions).
    private final Map<UUID, UUID> spectatorBindings = new HashMap<>();

    private BukkitTask task;
    private BukkitTask spectatorFollowTask;

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

        // A much shorter interval than the compass tick - this is what
        // keeps a bound spectator's camera glued to their teammate's
        // position smoothly as they move around.
        spectatorFollowTask = new BukkitRunnable() {
            @Override
            public void run() {
                tickSpectatorBindings();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (spectatorFollowTask != null) {
            spectatorFollowTask.cancel();
            spectatorFollowTask = null;
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

    // Which dimension a Hunter's current target is in, or "None" - used by
    // the scoreboard.
    public String getTargetDimension(Player hunter) {
        UUID targetId = targets.get(hunter.getUniqueId());
        if (targetId == null) return "None";
        Player target = Bukkit.getPlayer(targetId);
        return target != null ? formatDimension(target.getWorld().getEnvironment()) : "None";
    }

    public static String formatDimension(World.Environment environment) {
        return switch (environment) {
            case NETHER -> "Nether";
            case THE_END -> "The End";
            default -> "Overworld";
        };
    }

    // manhunt.permaDeathMode is a CYCLE setting - 0 = All Deaths, 1 = Hunter
    // Kills Only.
    public boolean isHunterKillsOnlyMode() {
        return RivalRun.getInstance().getConfig().getInt("manhunt.permaDeathMode", 0) == 1;
    }

    // Whether a Speedrunner who was just killed by killerRole should be
    // eliminated (converted to a spectator) rather than simply respawning.
    public boolean isPermanentDeath(TeamRole killerRole) {
        return !isHunterKillsOnlyMode() || killerRole == TeamRole.HUNTER;
    }

    public void markPermanentDeath(Player player) {
        permanentDeathPending.add(player.getUniqueId());
    }

    // True (and clears the flag) if the death that just happened to this
    // player was marked permanent in onDeath.
    public boolean consumePermanentDeath(Player player) {
        return permanentDeathPending.remove(player.getUniqueId());
    }

    public boolean hasBeenRevived(Player player) {
        return revivedOnce.contains(player.getUniqueId());
    }

    public void markRevived(Player player) {
        revivedOnce.add(player.getUniqueId());
    }

    // How much damage a Speedrunner team has dealt to the Ender Dragon this
    // match - used to fairly credit a win if a Hunter lands the kill.
    public void recordDragonDamage(Teams team, double damage) {
        dragonDamageByTeam.merge(team, damage, Double::sum);
    }

    // Picks which Speedrunner team should be credited with a dragon-kill
    // win when a Hunter lands the final blow: the sole surviving
    // Speedrunner team if there's only one, otherwise whichever team(s)
    // dealt the dragon the most damage this match (ties, including
    // "nobody hit it," broken randomly).
    public Teams pickDragonWinner() {
        RivalRun plugin = RivalRun.getInstance();
        List<Teams> alive = plugin.getTeamManager().getSpeedrunnerTeams().stream()
                .filter(t -> t.getSize() > 0)
                .toList();
        if (alive.isEmpty()) return null;
        if (alive.size() == 1) return alive.get(0);

        double best = -1;
        List<Teams> leaders = new ArrayList<>();
        for (Teams team : alive) {
            double damage = dragonDamageByTeam.getOrDefault(team, 0.0);
            if (damage > best) {
                best = damage;
                leaders.clear();
                leaders.add(team);
            } else if (damage == best) {
                leaders.add(team);
            }
        }

        return leaders.get(random.nextInt(leaders.size()));
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
    // spectator. If another Speedrunner team is still alive, this team
    // joins the Hunters at half health/saturation to help continue the
    // hunt. If this was the last Speedrunner team left, the game is simply
    // over - its members stay eliminated (spectating) rather than being
    // shuffled onto the winning team, since they were just caught, not
    // victors themselves.
    public void convertTeamToHunters(Teams eliminatedTeam) {
        RivalRun plugin = RivalRun.getInstance();
        Teams hunters = plugin.getTeamManager().getHunterTeam();
        if (hunters == null) return;

        boolean anySpeedrunnersLeft = plugin.getTeamManager().getSpeedrunnerTeams().stream()
                .anyMatch(t -> t != eliminatedTeam && t.getSize() > 0);

        if (!anySpeedrunnersLeft) {
            Bukkit.broadcast(Component.text(eliminatedTeam.getName() + " has been hunted down!", NamedTextColor.RED));
            plugin.getWinManager().announceWin(hunters, WinReason.HUNTERS_CAUGHT_ALL);
            return;
        }

        List<UUID> members = new ArrayList<>(eliminatedTeam.getPlayers());
        for (UUID uuid : members) {
            Player member = Bukkit.getPlayer(uuid);
            if (member == null) continue;

            unbindSpectator(member);
            plugin.getSpectatorManager().clearSpectator(member);
            plugin.getTeamManager().forceAssign(member, hunters);
            applyHalfState(member);
            giveTrackerCompass(member);
            member.sendRichMessage("<red>Your team was hunted down - you've joined the Hunters!");
        }

        Bukkit.broadcast(Component.text(eliminatedTeam.getName() + " has been hunted down and joins the Hunters!", NamedTextColor.RED));
    }

    // ---- Restricted spectating: a dead Speedrunner is locked to a living
    // teammate's position (can look around freely, but can't move or
    // interact - see ManhuntListener) rather than free-flying.

    // Auto-picks the first living teammate found.
    public void bindSpectatorToTeammate(Player spectator) {
        RivalRun plugin = RivalRun.getInstance();
        Teams team = plugin.getTeamManager().getPlayerTeam(spectator);
        if (team == null) {
            spectatorBindings.remove(spectator.getUniqueId());
            return;
        }

        for (UUID uuid : team.getPlayers()) {
            if (uuid.equals(spectator.getUniqueId())) continue;
            Player candidate = Bukkit.getPlayer(uuid);
            if (candidate != null && !plugin.getSpectatorManager().isSpectating(candidate)) {
                spectatorBindings.put(spectator.getUniqueId(), uuid);
                return;
            }
        }

        spectatorBindings.remove(spectator.getUniqueId());
    }

    // Explicit pick, e.g. from the spectate menu when more than one
    // teammate is still alive to choose from.
    public void bindSpectatorTo(Player spectator, Player teammate) {
        spectatorBindings.put(spectator.getUniqueId(), teammate.getUniqueId());
    }

    public void unbindSpectator(Player spectator) {
        spectatorBindings.remove(spectator.getUniqueId());
    }

    public boolean isBoundSpectator(Player player) {
        return spectatorBindings.containsKey(player.getUniqueId());
    }

    private void tickSpectatorBindings() {
        if (spectatorBindings.isEmpty()) return;

        RivalRun plugin = RivalRun.getInstance();
        if (!plugin.getGamemodeManager().isManhunt()) return;

        for (UUID spectatorId : new ArrayList<>(spectatorBindings.keySet())) {
            Player spectator = Bukkit.getPlayer(spectatorId);
            if (spectator == null) continue;

            UUID teammateId = spectatorBindings.get(spectatorId);
            Player teammate = teammateId != null ? Bukkit.getPlayer(teammateId) : null;

            // The bound teammate died/logged off since the last tick -
            // re-resolve to whichever teammate is still alive, if any.
            if (teammate == null || plugin.getSpectatorManager().isSpectating(teammate)) {
                bindSpectatorToTeammate(spectator);
                teammateId = spectatorBindings.get(spectatorId);
                teammate = teammateId != null ? Bukkit.getPlayer(teammateId) : null;
            }
            if (teammate == null) continue;

            Location current = spectator.getLocation();
            Location target = teammate.getLocation().clone();
            target.setYaw(current.getYaw());
            target.setPitch(current.getPitch());
            spectator.teleport(target);
        }
    }

    public void reset() {
        targets.clear();
        revivedOnce.clear();
        dragonDamageByTeam.clear();
        permanentDeathPending.clear();
        spectatorBindings.clear();
    }

}
