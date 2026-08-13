package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import xyz.skuller.rivalRun.helpers.Teams;

import java.time.Duration;
import java.util.UUID;

// Single entry point for "a team just won", used by both the Ender Dragon
// kill win condition and the spectator elimination win condition so the
// celebration (title, sound, fireworks) and state transition only live in
// one place.
public class WinManager {

    public void announceWin(Teams team) {
        RivalRun plugin = RivalRun.getInstance();
        GameStateManager gsm = plugin.getGameStateManager();

        if (gsm.isState(GameStateManager.GameStates.POST)) return;

        String timer = gsm.getFormattedElapsed();

        Component title = Component.text(team.getName() + " Wins!", team.getColor());
        Component subtitle = Component.text("Run time: " + timer, NamedTextColor.GRAY);
        Title displayTitle = Title.title(title, subtitle,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)));

        Bukkit.broadcast(Component.text("Team " + team.getName() + " has won the game! ", NamedTextColor.GOLD)
                .append(Component.text("(" + timer + ")", NamedTextColor.GRAY)));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(displayTitle);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }

        for (UUID uuid : team.getPlayers()) {
            Player winner = Bukkit.getPlayer(uuid);
            if (winner != null) {
                launchFirework(winner.getLocation());
            }
        }

        gsm.setState(GameStateManager.GameStates.POST);
    }

    private void launchFirework(Location location) {
        if (location.getWorld() == null) return;

        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.YELLOW, Color.ORANGE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .flicker(true)
                .build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }

}
