package xyz.skuller.rivalRun.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.Teams;

public class FriendlyFireEvent implements Listener {

    private final Plugin plugin;

    public FriendlyFireEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        boolean friendlyFire = plugin.getConfig().getBoolean("teams.friendlyFire");

        if (!friendlyFire) {
            Teams t1 = RivalRun.getInstance().getTeamManager().getPlayerTeam(damager);
            Teams t2 = RivalRun.getInstance().getTeamManager().getPlayerTeam(victim);

            if (t1 != null && t1 == t2) {
                event.setCancelled(true);
            }
        }
    }

}
