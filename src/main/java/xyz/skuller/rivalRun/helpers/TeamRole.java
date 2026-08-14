package xyz.skuller.rivalRun.helpers;

// A team's role within Manhunt. Classic teams are always NONE - the role
// only matters once Manhunt-specific logic (movement freeze, spectating
// restrictions, respawn rules, the tracking compass) needs to tell a
// Hunters team apart from a Speedrunner team.
public enum TeamRole {

    NONE,
    SPEEDRUNNER,
    HUNTER

}
