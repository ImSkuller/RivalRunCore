package xyz.skuller.rivalRun.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.TeamRole;
import xyz.skuller.rivalRun.helpers.Teams;
import xyz.skuller.rivalRun.managers.ManhuntManager;
import xyz.skuller.rivalRun.managers.SpectatorManager;
import xyz.skuller.rivalRun.managers.TeamsManager;
import xyz.skuller.rivalRun.menus.TeamSelectMenu;

public class TeamCommands {

    TeamsManager tm = RivalRun.getInstance().getTeamManager();

    public void selectTeam(Player player) {
        new TeamSelectMenu().open(player);
    }

    public void leaveTeam(Player player) {
        if (!(tm.isInTeam(player))) {
            player.sendRichMessage("<red>You cannot leave a team when you are not in one.");
        }
        else {
            tm.removePlayer(player);
            player.sendRichMessage("<green>You have now left <white>" + tm.getPlayerTeam(player) + "<red> Team.");
        }
    }

    public void switchTeams(Player player) {

        if(!(tm.isInTeam(player))){
            player.sendRichMessage("<red>You cannot switch teams when you are not in one.");
        }
        else {
            tm.removePlayer(player);
            new TeamSelectMenu().open(player);
            player.sendRichMessage("<green>If you couldn't join with the menu that just opened please do /rivalrun select");
        }
    }

    public void lockTeams(Player player) {
        tm.lockTeams();
        player.sendRichMessage("<red>Nobody can switch teams or join a team anymore.");
    }

    public void unlockTeams(Player player) {
        tm.unlockTeams();
        player.sendRichMessage("<red>Anyone can switch teams or join teams freely now. <gray>(This is not recommended to turn on while a speedrun game is running)");
    }

    // Manhunt only: revives a dead teammate, once per player per game -
    // available any time a Speedrunner team still has a living member to
    // call it, regardless of how many other Speedrunner teams remain.
    public void reviveTeammate(Player reviver, String targetName) {
        RivalRun plugin = RivalRun.getInstance();

        if (!plugin.getGamemodeManager().isManhunt()) {
            reviver.sendRichMessage("<red>Reviving teammates is a Manhunt-only feature.");
            return;
        }

        Teams team = tm.getPlayerTeam(reviver);
        if (team == null || team.getRole() != TeamRole.SPEEDRUNNER) {
            reviver.sendRichMessage("<red>Only Speedrunners can revive a teammate.");
            return;
        }

        SpectatorManager spectatorManager = plugin.getSpectatorManager();
        if (spectatorManager.isSpectating(reviver)) {
            reviver.sendRichMessage("<red>You cannot revive a teammate while you're eliminated yourself.");
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || tm.getPlayerTeam(target) != team) {
            reviver.sendRichMessage("<red>That's not one of your teammates.");
            return;
        }

        if (!spectatorManager.isSpectating(target)) {
            reviver.sendRichMessage("<red>" + target.getName() + " isn't eliminated.");
            return;
        }

        ManhuntManager manhuntManager = plugin.getManhuntManager();
        if (manhuntManager.hasBeenRevived(target)) {
            reviver.sendRichMessage("<red>" + target.getName() + " has already used their one revival this game.");
            return;
        }

        manhuntManager.unbindSpectator(target);
        spectatorManager.clearSpectator(target);
        target.teleport(reviver.getLocation());
        manhuntManager.applyHalfState(target);
        manhuntManager.markRevived(target);

        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                "<green>" + target.getName() + " was revived by their team!"));
    }
}
