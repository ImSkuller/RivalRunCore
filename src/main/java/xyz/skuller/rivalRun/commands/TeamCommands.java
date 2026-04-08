package xyz.skuller.rivalRun.commands;

import org.bukkit.entity.Player;
import xyz.skuller.rivalRun.RivalRun;
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
}
