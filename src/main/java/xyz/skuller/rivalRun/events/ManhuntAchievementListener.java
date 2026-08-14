package xyz.skuller.rivalRun.events;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.AchievementType;
import xyz.skuller.rivalRun.helpers.TeamRole;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.AchievementManager;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.ManhuntManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

// Manhunt's 30 achievements - the ones driven by combat/exploration events.
// Win-time ones (Speed Demon, Quick Hunt, Full Squad, ...) live in
// AchievementManager.onWin() instead, since they need the winning team and
// final match state rather than a single event.
public class ManhuntAchievementListener implements Listener {

    private boolean active() {
        RivalRun plugin = RivalRun.getInstance();
        return plugin.getGamemodeManager().isManhunt()
                && plugin.getGameStateManager().isState(GameStateManager.GameStates.RUNNING);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!active()) return;

        RivalRun plugin = RivalRun.getInstance();
        TeamsManager tm = plugin.getTeamManager();
        AchievementManager am = plugin.getAchievementManager();
        ManhuntManager mm = plugin.getManhuntManager();

        Player victim = event.getEntity();
        Teams victimTeam = tm.getPlayerTeam(victim);
        TeamRole victimRole = tm.getTeamRole(victim);
        am.recordDeath(victimTeam);

        if (victimRole == TeamRole.HUNTER) {
            mm.markHunterDied();
        }

        Player killer = victim.getKiller();
        if (killer == null) return;

        Teams killerTeam = tm.getPlayerTeam(killer);
        TeamRole killerRole = tm.getTeamRole(killer);
        if (killerTeam == null || killerRole == TeamRole.NONE || victimRole == TeamRole.NONE || killerRole == victimRole) {
            return;
        }

        mm.recordCrossKill(killerRole, victimRole);
        if (mm.isBloodFeud()) {
            am.award(killerTeam, AchievementType.BLOOD_FEUD);
        }

        if (mm.recordKillAndCheckIronGrip(killer, victim)) {
            am.award(killerTeam, AchievementType.IRON_GRIP);
        }

        if (killerRole == TeamRole.HUNTER) {
            am.award(killerTeam, AchievementType.FIRST_BLOOD);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!active()) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) return;

        RivalRun plugin = RivalRun.getInstance();
        TeamsManager tm = plugin.getTeamManager();
        AchievementManager am = plugin.getAchievementManager();
        GameStateManager gsm = plugin.getGameStateManager();

        TeamRole attackerRole = tm.getTeamRole(attacker);
        TeamRole victimRole = tm.getTeamRole(victim);
        Teams attackerTeam = tm.getPlayerTeam(attacker);
        if (attackerTeam == null || attackerRole == TeamRole.NONE || victimRole == TeamRole.NONE || attackerRole == victimRole) {
            return;
        }

        double maxHealth = victim.getAttribute(Attribute.MAX_HEALTH).getValue();
        boolean lethal = event.getFinalDamage() >= victim.getHealth();
        boolean lowHealth = victim.getHealth() <= maxHealth * 0.25;

        if (attackerRole == TeamRole.HUNTER && victimRole == TeamRole.SPEEDRUNNER) {
            am.award(attackerTeam, AchievementType.FIRST_STRIKE);

            if (gsm.getElapsedSeconds() <= 60) {
                am.award(attackerTeam, AchievementType.FAST_START);
            }

            if (lethal && lowHealth) {
                am.award(attackerTeam, AchievementType.AMBUSH);
            }
            if (lethal && attacker.getInventory().getItemInMainHand().getType() == Material.AIR) {
                am.award(attackerTeam, AchievementType.NO_MERCY);
            }
            if (lethal && event.getDamager() instanceof Projectile) {
                am.award(attackerTeam, AchievementType.MARKSMAN);
            }
            if (!lethal && lowHealth) {
                Teams victimTeam = tm.getPlayerTeam(victim);
                if (victimTeam != null) am.award(victimTeam, AchievementType.GREAT_ESCAPE);
            }
        }

        if (lethal && event.getFinalDamage() >= 15.0) {
            am.award(attackerTeam, AchievementType.ONE_SHOT);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!active()) return;
        if (event.getRecipe().getResult().getType() != Material.SHIELD) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        award(player, AchievementType.PREPARED);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!active()) return;
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        award(event.getPlayer(), AchievementType.EVASIVE_MANEUVERS);
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!active()) return;
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;

        award(event.getPlayer(), AchievementType.HOUSEWARMING);
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    private void award(Player player, AchievementType type) {
        Teams team = RivalRun.getInstance().getTeamManager().getPlayerTeam(player);
        if (team == null) return;
        if (team.getRole() != TeamRole.SPEEDRUNNER) return;

        RivalRun.getInstance().getAchievementManager().award(team, type);
    }

}
