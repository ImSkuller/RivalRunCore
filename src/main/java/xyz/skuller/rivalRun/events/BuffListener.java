package xyz.skuller.rivalRun.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.managers.BuffManager;
import xyz.skuller.rivalRun.managers.GameStateManager;

// Applies the effect-driven half of the per-player buff/debuff system: PvP
// damage multipliers, on-kill Regeneration/Strength rewards, and keeping
// the "permanent" buffs (health, speed) actually in effect across respawns
// and late joins. The config-editable side lives in BuffManager/
// SettingsCategories.playerBuffs.
public class BuffListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!isRunning()) return;

        BuffManager buffs = RivalRun.getInstance().getBuffManager();
        double multiplier = buffs.getDamageDealtMultiplier(attacker) * buffs.getDamageTakenMultiplier(victim);
        event.setDamage(event.getDamage() * multiplier);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getPlayer().getKiller();
        if (killer == null) return;
        if (!isRunning()) return;

        BuffManager buffs = RivalRun.getInstance().getBuffManager();

        int regenSeconds = buffs.getKillRegenSeconds(killer);
        if (regenSeconds > 0) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenSeconds * 20, 0, false, true));
        }

        int strengthSeconds = buffs.getKillStrengthSeconds(killer);
        if (strengthSeconds > 0) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, strengthSeconds * 20, 0, false, true));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        RivalRun plugin = RivalRun.getInstance();
        if (!plugin.getBuffManager().isActive() || !isRunning()) return;

        // Potion effects don't survive death (attributes do), so Speed/
        // Slowness needs reapplying; the attribute call is a harmless no-op.
        plugin.getBuffManager().applyPermanentBuffs(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Always reconcile, not just apply - clearAll()/applyAll() can only
        // reach players who were online at the time, so a player who
        // disconnected mid-buff needs their attributes corrected either way
        // on the way back in (matching the current buffs, or reset to
        // normal if no game is active).
        BuffManager buffs = RivalRun.getInstance().getBuffManager();

        if (buffs.isActive() && isRunning()) {
            buffs.applyPermanentBuffs(event.getPlayer());
        } else {
            buffs.clearPermanentBuffs(event.getPlayer());
        }
    }

    private boolean isRunning() {
        return RivalRun.getInstance().getGameStateManager().isState(GameStateManager.GameStates.RUNNING);
    }

}
