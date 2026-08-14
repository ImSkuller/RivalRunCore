package xyz.skuller.rivalRun.events;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.AchievementType;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.AchievementManager;

import java.util.EnumSet;
import java.util.Set;

// Classic's 30 achievements - the original 6 route milestones plus 24 more
// mining/gearing/combat/exploration ones. Manhunt has its own separate set
// (see ManhuntAchievementListener) - AchievementManager.award() already
// filters by the active gamemode, but every handler here is harmless to
// leave registered during a Manhunt game since none of its triggers award
// Manhunt-tagged types.
public class AchievementListener implements Listener {

    private static final Set<Material> CROPS = EnumSet.of(
            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS);

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
        } else if (material == Material.OBSIDIAN) {
            award(player, AchievementType.OBSIDIAN_COLLECTED);
        } else if (material == Material.NETHER_WART) {
            award(player, AchievementType.NETHER_WART_COLLECTED);
        } else if (material == Material.NETHER_STAR) {
            award(player, AchievementType.NETHER_STAR_COLLECTED);
        } else if (material == Material.GLASS_BOTTLE) {
            award(player, AchievementType.GLASS_BOTTLE_COLLECTED);
        } else if (material == Material.NETHERITE_SCRAP) {
            award(player, AchievementType.NETHERITE_SCRAP_COLLECTED);
        } else if (material == Material.NETHERITE_INGOT) {
            award(player, AchievementType.NETHERITE_INGOT_COLLECTED);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Material result = event.getRecipe().getResult().getType();
        if (result == Material.ENDER_EYE) {
            award(player, AchievementType.EYE_OF_ENDER);
        } else if (result == Material.IRON_PICKAXE) {
            award(player, AchievementType.IRON_PICKAXE);
        } else if (result == Material.DIAMOND_PICKAXE) {
            award(player, AchievementType.DIAMOND_PICKAXE);
        } else if (result == Material.DIAMOND_SWORD) {
            award(player, AchievementType.BLACKSMITH);
        }
    }

    @EventHandler
    public void onDragonDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        award(player, AchievementType.DAMAGE_DRAGON);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        Player player = event.getPlayer();

        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            award(player, AchievementType.DIAMONDS);
        } else if (CROPS.contains(type)) {
            award(player, AchievementType.GREEN_THUMB);
        }
    }

    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        var armor = player.getInventory().getArmorContents();

        boolean fullDiamond = true;
        boolean fullIron = true;
        for (var piece : armor) {
            Material type = piece != null ? piece.getType() : Material.AIR;
            if (!type.name().startsWith("DIAMOND_")) fullDiamond = false;
            if (!type.name().startsWith("IRON_")) fullIron = false;
        }

        if (fullDiamond) award(player, AchievementType.FULL_DIAMOND_ARMOR);
        if (fullIron) award(player, AchievementType.FULL_IRON_ARMOR);
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        award(event.getEnchanter(), AchievementType.ENCHANTED_ITEM);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Material type = event.getItem().getType();
        if (type == Material.POTION) {
            award(event.getPlayer(), AchievementType.POTION_CONSUMED);
        } else if (type == Material.ROTTEN_FLESH) {
            award(event.getPlayer(), AchievementType.IRON_BELLY);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof org.bukkit.entity.ZombieVillager)) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.GOLDEN_APPLE) return;

        award(event.getPlayer(), AchievementType.ZOMBIE_CURE_STARTED);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Entity entity = event.getEntity();

        if (entity instanceof Skeleton && killer.getLocation().distance(entity.getLocation()) >= 20) {
            award(killer, AchievementType.SNIPER_DUEL);
        }
        if (entity instanceof Enderman) {
            award(killer, AchievementType.ENDER_BUSINESS);
        }
        if (entity instanceof Monster) {
            award(killer, AchievementType.MONSTER_HUNTER);
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        award(event.getPlayer(), AchievementType.MASTER_ANGLER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || event.getTo().getY() < 256) return;
        award(event.getPlayer(), AchievementType.SKY_HIGH);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getFinalDamage() < 10 || player.getHealth() - event.getFinalDamage() <= 0) return;

        award(player, AchievementType.FREE_FALLER);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (RivalRun.getInstance().getGamemodeManager().isManhunt()) return;

        Teams team = RivalRun.getInstance().getTeamManager().getPlayerTeam(event.getEntity());
        RivalRun.getInstance().getAchievementManager().recordDeath(team);
    }

    private void award(Player player, AchievementType type) {
        Teams team = RivalRun.getInstance().getTeamManager().getPlayerTeam(player);
        if (team == null) return;

        AchievementManager achievements = RivalRun.getInstance().getAchievementManager();
        achievements.award(team, type);
    }

}
