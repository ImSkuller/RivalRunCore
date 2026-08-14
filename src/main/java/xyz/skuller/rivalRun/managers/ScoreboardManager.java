package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.TeamRole;
import xyz.skuller.rivalRun.helpers.Teams;

import java.util.ArrayList;
import java.util.List;

// Owns every player's sidebar scoreboard AND the per-team color/prefix setup
// that colors nametags + the tab list. Both need to live on the same
// Scoreboard instance a player is looking at, so giving each player their
// own personal Scoreboard for the sidebar means the team registrations have
// to be rebuilt onto that same instance rather than the shared main
// scoreboard - otherwise nametag coloring would silently stop working the
// moment a personalized sidebar is attached.
public class ScoreboardManager {

    private static final String OBJECTIVE_ID = "rivalrun";
    private static final int MAX_LINES = 15;

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

    public void refresh(Player viewer) {
        RivalRun plugin = RivalRun.getInstance();
        TeamsManager tm = plugin.getTeamManager();

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        // Color teams (drives both nametag color and tab list prefix)
        for (Teams team : tm.getTeams()) {
            Team sbTeam = board.registerNewTeam(team.getName());
            sbTeam.color(team.getColor());
            sbTeam.prefix(Component.text(team.getName().toUpperCase() + " ", team.getColor())
                    .decoration(TextDecoration.BOLD, true));

            for (var uuid : team.getPlayers()) {
                Player member = Bukkit.getPlayer(uuid);
                if (member != null) {
                    String suffix = plugin.getSpectatorManager().isSpectating(member) ? " (Spectating)" : "";
                    if (!suffix.isEmpty()) sbTeam.suffix(Component.text(suffix, NamedTextColor.GRAY));
                    sbTeam.addEntry(member.getName());
                }
            }
        }

        if (plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            boolean manhunt = plugin.getGamemodeManager().isManhunt();
            String titlePath = manhunt ? "scoreboard.manhunt.title" : "scoreboard.classic.title";
            String titleDefault = manhunt ? "<red><bold>MANHUNT" : "<gold><bold>RIVAL RUN";
            String titleTemplate = plugin.getConfig().getString(titlePath, titleDefault);
            Objective objective = board.registerNewObjective(OBJECTIVE_ID, Criteria.DUMMY, MiniMessage.miniMessage().deserialize(titleTemplate));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            List<Component> lines = manhunt ? buildManhuntLines(viewer) : buildClassicLines(viewer);
            int score = lines.size();

            for (int i = 0; i < lines.size() && i < MAX_LINES; i++) {
                String entry = blankEntry(i);
                Team lineTeam = board.registerNewTeam("rr_line_" + i);
                lineTeam.addEntry(entry);
                lineTeam.prefix(lines.get(i));
                objective.getScore(entry).setScore(score--);
            }
        }

        viewer.setScoreboard(board);
    }

    private List<Component> buildClassicLines(Player viewer) {
        RivalRun plugin = RivalRun.getInstance();
        GameStateManager gsm = plugin.getGameStateManager();
        TeamsManager tm = plugin.getTeamManager();
        SpectatorManager sm = plugin.getSpectatorManager();
        AchievementManager am = plugin.getAchievementManager();

        List<Component> lines = new ArrayList<>();

        lines.add(Component.text("State: ", NamedTextColor.GRAY).append(Component.text(gsm.getState().name(), NamedTextColor.WHITE)));
        lines.add(Component.text("Timer: ", NamedTextColor.GRAY).append(Component.text(gsm.getFormattedElapsed(), NamedTextColor.WHITE)));
        lines.add(Component.empty());

        Teams viewerTeam = tm.getPlayerTeam(viewer);
        lines.add(Component.text("Your Team: ", NamedTextColor.GRAY).append(
                viewerTeam != null
                        ? Component.text(viewerTeam.getName().toUpperCase(), viewerTeam.getColor(), TextDecoration.BOLD)
                        : Component.text("None", NamedTextColor.WHITE)
        ));
        lines.add(Component.empty());

        for (Teams team : tm.getTeams()) {
            int alive = sm.countAlive(team);
            long achievements = am.countFor(team);

            Component line = Component.text("■ ", team.getColor())
                    .append(Component.text(team.getName() + " ", team.getColor()))
                    .append(Component.text(alive + "/" + team.getSize(), NamedTextColor.WHITE));

            if (achievements > 0) {
                line = line.append(Component.text("  ⭐" + achievements, NamedTextColor.YELLOW));
            }

            lines.add(line);
        }

        return lines;
    }

    private List<Component> buildManhuntLines(Player viewer) {
        RivalRun plugin = RivalRun.getInstance();
        GameStateManager gsm = plugin.getGameStateManager();
        TeamsManager tm = plugin.getTeamManager();
        SpectatorManager sm = plugin.getSpectatorManager();

        List<Component> lines = new ArrayList<>();

        lines.add(Component.text("State: ", NamedTextColor.GRAY).append(Component.text(gsm.getState().name(), NamedTextColor.WHITE)));
        lines.add(Component.text("Timer: ", NamedTextColor.GRAY).append(Component.text(gsm.getFormattedElapsed(), NamedTextColor.WHITE)));
        lines.add(Component.empty());

        TeamRole role = tm.getTeamRole(viewer);
        Component roleText = switch (role) {
            case HUNTER -> Component.text("HUNTER", NamedTextColor.RED, TextDecoration.BOLD);
            case SPEEDRUNNER -> Component.text("SPEEDRUNNER", NamedTextColor.AQUA, TextDecoration.BOLD);
            case NONE -> Component.text("None", NamedTextColor.WHITE);
        };
        lines.add(Component.text("Role: ", NamedTextColor.GRAY).append(roleText));
        lines.add(Component.empty());

        Teams hunters = tm.getHunterTeam();
        if (hunters != null) {
            lines.add(Component.text("■ ", hunters.getColor())
                    .append(Component.text("Hunters ", hunters.getColor()))
                    .append(Component.text(hunters.getSize(), NamedTextColor.WHITE)));
        }

        for (Teams team : tm.getSpeedrunnerTeams()) {
            int alive = sm.countAlive(team);
            lines.add(Component.text("■ ", team.getColor())
                    .append(Component.text(team.getName() + " ", team.getColor()))
                    .append(Component.text(alive + "/" + team.getSize(), NamedTextColor.WHITE)));
        }

        if (role == TeamRole.HUNTER) {
            lines.add(Component.empty());
            lines.add(Component.text("Tracking: ", NamedTextColor.GRAY)
                    .append(Component.text(plugin.getManhuntManager().getTargetName(viewer), NamedTextColor.RED)));
        }

        return lines;
    }

    // Sidebar lines are keyed by a unique "entry" string per score. Legacy
    // color codes make for cheap, invisible, guaranteed-unique entries.
    private String blankEntry(int index) {
        return "§" + Integer.toHexString(index & 0xF) + (index >= 16 ? Integer.toHexString((index >> 4) & 0xF) : "");
    }

}
