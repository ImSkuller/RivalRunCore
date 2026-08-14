package xyz.skuller.rivalRun.helpers;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

// A snapshot of a completed match, taken at the moment a team wins - teams
// and alive counts get wiped by the next reset, so this is what
// /rivalrun summary reads from afterward rather than live state.
public record MatchSummary(
        String winningTeamName,
        NamedTextColor winningTeamColor,
        WinReason reason,
        String runTime,
        List<TeamResult> teamResults
) {

    public record TeamResult(
            String name,
            NamedTextColor color,
            int alive,
            int total
    ) {}

}
