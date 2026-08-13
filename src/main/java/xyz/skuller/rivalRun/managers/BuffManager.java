package xyz.skuller.rivalRun.managers;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.skuller.rivalRun.RivalRun;

// Per-player handicap system: max health, hunger drain rate, passive
// speed/slowness, and PvP damage-dealt/damage-taken multipliers are all
// "permanent" for the duration of a run; on-kill Regeneration/Strength are
// temporary rewards handled by BuffListener instead. Settings persist in
// config.yml under buffs.<uuid>.* across resets and restarts - only the
// *live effects* are applied/cleared per game, via applyAll()/clearAll().
public class BuffManager {

    private static final double BASE_MAX_HEALTH = 20.0;
    private static final double[] SATURATION_EXHAUSTION_DELTA = {-0.6, -0.3, 0.0, 0.3, 0.6};

    private BukkitTask task;
    private boolean active;

    public void applyAll() {
        active = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyPermanentBuffs(player);
        }
        startTask();
    }

    public void clearAll() {
        active = false;
        stopTask();
        for (Player player : Bukkit.getOnlinePlayers()) {
            clearPermanentBuffs(player);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void applyPermanentBuffs(Player player) {
        applyHealth(player);
        applySpeed(player);
    }

    public void clearPermanentBuffs(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(BASE_MAX_HEALTH);
            if (player.getHealth() > BASE_MAX_HEALTH) {
                player.setHealth(BASE_MAX_HEALTH);
            }
        }

        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }

    // Always sets an explicit value (rather than skipping when hearts == 0)
    // so a player who disconnected mid-buff and reconnects after their
    // config changed (or was cleared) doesn't keep a stale attribute value -
    // clearAll()/applyAll() can only reach players who are online at the time.
    private void applyHealth(Player player) {
        int hearts = getInt(player, "health", 0);

        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;

        double newMax = Math.max(2.0, BASE_MAX_HEALTH + (hearts * 2.0));
        attribute.setBaseValue(newMax);
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }

    private void applySpeed(Player player) {
        int index = getInt(player, "speed", 2);
        if (index == 2) return;

        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.SLOWNESS);

        PotionEffectType type = index < 2 ? PotionEffectType.SLOWNESS : PotionEffectType.SPEED;
        int amplifier = index < 2 ? (1 - index) : (index - 3);
        player.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, amplifier, true, false));
    }

    public double getDamageDealtMultiplier(Player player) {
        return tierMultiplier(getInt(player, "damageDealt", 2));
    }

    public double getDamageTakenMultiplier(Player player) {
        return tierMultiplier(getInt(player, "damageTaken", 2));
    }

    private double tierMultiplier(int index) {
        return switch (index) {
            case 0 -> 0.75;
            case 1 -> 0.9;
            case 3 -> 1.1;
            case 4 -> 1.25;
            default -> 1.0;
        };
    }

    public int getKillRegenSeconds(Player player) {
        return getInt(player, "killRegenSeconds", 0);
    }

    public int getKillStrengthSeconds(Player player) {
        return getInt(player, "killStrengthSeconds", 0);
    }

    private int getInt(Player player, String key, int def) {
        return RivalRun.getInstance().getConfig().getInt("buffs." + player.getUniqueId() + "." + key, def);
    }

    private void startTask() {
        stopTask();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(RivalRun.getInstance(), 20L, 20L);
    }

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        if (!active) return;
        if (!RivalRun.getInstance().getGameStateManager().isState(GameStateManager.GameStates.RUNNING)) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            reapplySpeedIfMissing(player);
            nudgeSaturation(player);
        }
    }

    // Milk buckets (and anything else that clears effects) would otherwise
    // silently undo a Speed/Slowness handicap for the rest of the game.
    private void reapplySpeedIfMissing(Player player) {
        int index = getInt(player, "speed", 2);
        if (index == 2) return;

        boolean hasSlowness = player.hasPotionEffect(PotionEffectType.SLOWNESS);
        boolean hasSpeed = player.hasPotionEffect(PotionEffectType.SPEED);

        if ((index < 2 && !hasSlowness) || (index > 2 && !hasSpeed)) {
            applySpeed(player);
        }
    }

    private void nudgeSaturation(Player player) {
        int index = getInt(player, "saturationRate", 2);
        if (index == 2) return;

        double delta = SATURATION_EXHAUSTION_DELTA[index];
        float newExhaustion = (float) Math.max(0.0, Math.min(4.0, player.getExhaustion() + delta));
        player.setExhaustion(newExhaustion);
    }

}
