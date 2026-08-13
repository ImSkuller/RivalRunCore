package xyz.skuller.rivalRun.events;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.managers.GameStateManager;

public class MotdEvent implements Listener {

    @EventHandler
    public void onPing(PaperServerListPingEvent event) {
        RivalRun plugin = RivalRun.getInstance();

        if (!plugin.getConfig().getBoolean("motd.enabled", true)) return;

        GameStateManager gsm = plugin.getGameStateManager();
        String path = switch (gsm.getState()) {
            case WAITING -> "motd.waiting";
            case STARTING -> "motd.starting";
            case RUNNING, PAUSED -> "motd.running";
            case POST -> "motd.post";
        };

        String template = plugin.getConfig().getString(path, "");
        String replaced = template
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getServer().getMaxPlayers()))
                .replace("{timer}", gsm.getFormattedElapsed());

        Component motd = MiniMessage.miniMessage().deserialize(replaced);
        event.motd(motd);
    }

}
