package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.TeamPresets;
import xyz.skuller.rivalRun.helpers.Teams;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.UUID;

// Single entry point for "a team just won", used by both the Ender Dragon
// kill win condition and the spectator elimination win condition so the
// celebration (chat banner, titles, sounds, fireworks) and state
// transition only live in one place.
public class WinManager {

    private static final Component DIVIDER = Component.text(
            "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.GOLD, TextDecoration.BOLD);

    private final Random random = new Random();

    public void announceWin(Teams team) {
        RivalRun plugin = RivalRun.getInstance();
        GameStateManager gsm = plugin.getGameStateManager();

        if (gsm.isState(GameStateManager.GameStates.POST)) return;

        gsm.setState(GameStateManager.GameStates.POST);

        String timer = gsm.getFormattedElapsed();
        List<String> winnerNames = plugin.getTeamManager().getPlayerNames(team);

        broadcastBanner(team, timer, winnerNames);
        showTitles(team, timer);
        playSounds(team);
        launchFireworkShow(plugin, team);
    }

    private void broadcastBanner(Teams team, String timer, List<String> winnerNames) {
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(DIVIDER);
        Bukkit.broadcast(Component.text("  🏆  TEAM ", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(team.getName().toUpperCase(), team.getColor(), TextDecoration.BOLD))
                .append(Component.text(" WINS!  🏆", NamedTextColor.GOLD, TextDecoration.BOLD)));
        Bukkit.broadcast(Component.text("  Run time: ", NamedTextColor.GRAY)
                .append(Component.text(timer, NamedTextColor.WHITE)));

        if (!winnerNames.isEmpty()) {
            Bukkit.broadcast(Component.text("  Winners: ", NamedTextColor.GRAY)
                    .append(Component.text(String.join(", ", winnerNames), team.getColor())));
        }

        Bukkit.broadcast(DIVIDER);
        Bukkit.broadcast(Component.empty());
    }

    private void showTitles(Teams team, String timer) {
        Component winnerTitle = Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD);
        Component winnerSubtitle = Component.text("Your team won in " + timer + "!", NamedTextColor.GREEN);
        Title winnerDisplay = Title.title(winnerTitle, winnerSubtitle,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)));

        Component othersTitle = Component.text(team.getName() + " Wins!", team.getColor(), TextDecoration.BOLD);
        Component othersSubtitle = Component.text("Run time: " + timer, NamedTextColor.GRAY);
        Title othersDisplay = Title.title(othersTitle, othersSubtitle,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)));

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean isWinner = team.getPlayers().contains(player.getUniqueId());
            player.showTitle(isWinner ? winnerDisplay : othersDisplay);
            player.sendActionBar(Component.text(team.getName() + " has won the game!", team.getColor()));
        }
    }

    private void playSounds(Teams team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

            if (team.getPlayers().contains(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        }
    }

    // Three staggered bursts per winner instead of one static firework, for a
    // proper little fireworks show rather than a single pop.
    private void launchFireworkShow(RivalRun plugin, Teams team) {
        for (UUID uuid : team.getPlayers()) {
            Player winner = Bukkit.getPlayer(uuid);
            if (winner == null) continue;

            Location location = winner.getLocation();
            for (int burst = 0; burst < 3; burst++) {
                long delay = burst * 12L;
                Bukkit.getScheduler().runTaskLater(plugin, () -> launchFirework(location, team.getColor()), delay);
            }
        }
    }

    private void launchFirework(Location location, NamedTextColor teamColor) {
        if (location.getWorld() == null) return;

        FireworkEffect.Type[] shapes = FireworkEffect.Type.values();
        Color primary = TeamPresets.getFireworkColorByColor(teamColor);

        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(primary, Color.WHITE)
                .withFade(Color.YELLOW)
                .with(shapes[random.nextInt(shapes.length)])
                .trail(true)
                .flicker(true)
                .build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }

}
