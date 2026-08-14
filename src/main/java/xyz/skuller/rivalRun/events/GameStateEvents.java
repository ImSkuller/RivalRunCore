package xyz.skuller.rivalRun.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.TeamRole;
import xyz.skuller.rivalRun.managers.GameStateManager;

public class GameStateEvents implements Listener {

    private final GameStateManager gsm;

    public GameStateEvents(GameStateManager gsm) {
        this.gsm = gsm;
    }

    // True before the run is actually live (still in the lobby or the
    // pre-game countdown) - blocks/entities/PvP/hunger should all be inert.
    private boolean isPreGame() {
        return gsm.isState(GameStateManager.GameStates.WAITING)
                || gsm.isState(GameStateManager.GameStates.STARTING);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if (gsm.isState(GameStateManager.GameStates.PAUSED)) {
            event.getPlayer().sendRichMessage("<red>You cannot break blocks right now as the game is paused.");
            event.setCancelled(true);
        }

        if (isPreGame()) {
            event.getPlayer().sendRichMessage("<red>You cannot break blocks right now as the game has not started yet.");
            event.setCancelled(true);
        }

        if (gsm.isState(GameStateManager.GameStates.POST)) {
            event.getPlayer().sendRichMessage("<red>You cannot break blocks right now as the game has ended.");
            event.setCancelled(true);
        }

    }

    // Blocks (chests, doors, buttons, crafting tables, ...) and entities
    // (villagers, item frames, ...) can't be interacted with before the run
    // is live - no looting a chest you happened to spawn next to.
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isPreGame()) return;
        if (event.getClickedBlock() == null) return;

        event.setCancelled(true);
        event.getPlayer().sendRichMessage("<red>You cannot interact with anything right now as the game has not started yet.");
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!isPreGame()) return;

        event.setCancelled(true);
        event.getPlayer().sendRichMessage("<red>You cannot interact with anything right now as the game has not started yet.");
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {

        if (gsm.isState(GameStateManager.GameStates.PAUSED) ||
                isPreGame() ||
                gsm.isState(GameStateManager.GameStates.POST))
        {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        if (gsm.isState(GameStateManager.GameStates.PAUSED)) {
            event.getPlayer().sendRichMessage("<red>The game is paused please refrain from dropping items.");
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (gsm.isGracePeriod() || isPreGame()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        if (gsm.isState(GameStateManager.GameStates.PAUSED)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (gsm.isState(GameStateManager.GameStates.PAUSED) ||
                gsm.isState(GameStateManager.GameStates.STARTING))
        {
            freeze(event, Component.text("Game is " + gsm.getState(), NamedTextColor.RED));
            return;
        }

        RivalRun plugin = RivalRun.getInstance();
        if (gsm.isHeadstart()
                && plugin.getGamemodeManager().isManhunt()
                && plugin.getTeamManager().getTeamRole(event.getPlayer()) == TeamRole.HUNTER)
        {
            freeze(event, Component.text("Headstart! You can look around but not move yet.", NamedTextColor.RED));
        }
    }

    // Locks horizontal (X/Z) movement while leaving looking around (and
    // vertical movement, e.g. falling/jumping in place) untouched.
    private void freeze(PlayerMoveEvent event, Component message) {
        if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getZ() != event.getTo().getZ())
        {
            event.setTo(event.getFrom());
            event.getPlayer().sendActionBar(message);
        }
    }

}
