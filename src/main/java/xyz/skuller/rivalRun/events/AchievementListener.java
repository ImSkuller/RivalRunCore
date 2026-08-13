package xyz.skuller.rivalRun.events;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.AchievementType;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.AchievementManager;

// Fires the six team-speedrunning route achievements as players hit them.
public class AchievementListener implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World.Environment environment = player.getWorld().getEnvironment();

        if (environment == World.Environment.NETHER) {
            award(player, AchievementType.NETHER);
        } else if (environment == World.Environment.THE_END) {
            award(player, AchievementType.ENTER_END);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Material material = event.getItem().getItemStack().getType();
        if (material == Material.BLAZE_ROD) {
            award(player, AchievementType.BLAZE_ROD);
        } else if (material == Material.ENDER_PEARL) {
            award(player, AchievementType.ENDER_PEARL);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRecipe().getResult().getType() != Material.ENDER_EYE) return;

        award(player, AchievementType.EYE_OF_ENDER);
    }

    @EventHandler
    public void onDragonDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        award(player, AchievementType.DAMAGE_DRAGON);
    }

    private void award(Player player, AchievementType type) {
        Teams team = RivalRun.getInstance().getTeamManager().getPlayerTeam(player);
        if (team == null) return;

        AchievementManager achievements = RivalRun.getInstance().getAchievementManager();
        achievements.award(team, type);
    }

}
