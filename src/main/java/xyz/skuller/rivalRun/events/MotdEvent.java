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
        String mode = plugin.getGamemodeManager().isManhunt() ? "manhunt" : "classic";
        String stateKey = switch (gsm.getState()) {
            case WAITING -> "waiting";
            case STARTING -> "starting";
            case RUNNING, PAUSED -> "running";
            case POST -> "post";
        };
        String path = "motd." + mode + "." + stateKey;

        int speedrunnersAlive = plugin.getTeamManager().getSpeedrunnerTeams().stream()
                .mapToInt(plugin.getSpectatorManager()::countAlive).sum();
        var hunterTeam = plugin.getTeamManager().getHunterTeam();
        int huntersAlive = hunterTeam != null ? hunterTeam.getSize() : 0;

        String template = plugin.getConfig().getString(path, "");
        String replaced = template
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getServer().getMaxPlayers()))
                .replace("{timer}", gsm.getFormattedElapsed())
                .replace("{speedrunners_alive}", String.valueOf(speedrunnersAlive))
                .replace("{hunters_alive}", String.valueOf(huntersAlive));

        Component motd = MiniMessage.miniMessage().deserialize(replaced);
        event.motd(motd);
    }

}
