package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.skuller.rivalRun.RivalRun;

// Drives the tab list header/footer. Team name coloring itself lives in
// ScoreboardManager, since it has to be set on the same per-player
// Scoreboard/Team objects that the sidebar uses.
public class TabListManager {

    private BukkitTask task;

    public void start(RivalRun plugin) {
        stop();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                refreshAll();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void refreshAll() {
        Bukkit.getOnlinePlayers().forEach(this::refresh);
    }

    public void refresh(Player player) {
        RivalRun plugin = RivalRun.getInstance();

        if (!plugin.getConfig().getBoolean("tablist.enabled", true)) {
            player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
            return;
        }

        String mode = plugin.getGamemodeManager().isManhunt() ? "manhunt" : "classic";
        Component header = render(plugin.getConfig().getString("tablist." + mode + ".header", ""));
        Component footer = render(plugin.getConfig().getString("tablist." + mode + ".footer", ""));

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    private Component render(String template) {
        RivalRun plugin = RivalRun.getInstance();
        GameStateManager gsm = plugin.getGameStateManager();
        TeamsManager tm = plugin.getTeamManager();
        SpectatorManager sm = plugin.getSpectatorManager();

        int speedrunnersAlive = tm.getSpeedrunnerTeams().stream().mapToInt(sm::countAlive).sum();
        int huntersAlive = tm.getHunterTeam() != null ? tm.getHunterTeam().getSize() : 0;

        String replaced = template
                .replace("{state}", gsm.getState().name())
                .replace("{timer}", gsm.getFormattedElapsed())
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getServer().getMaxPlayers()))
                .replace("{speedrunners_alive}", String.valueOf(speedrunnersAlive))
                .replace("{hunters_alive}", String.valueOf(huntersAlive));

        return MiniMessage.miniMessage().deserialize(replaced);
    }

}
