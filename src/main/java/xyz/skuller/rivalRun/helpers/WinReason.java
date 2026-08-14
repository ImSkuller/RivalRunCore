package xyz.skuller.rivalRun.helpers;

public enum WinReason {

    DRAGON("Defeated the Ender Dragon"),
    ELIMINATION("Last team standing"),
    HUNTERS_CAUGHT_ALL("Hunted down every last Speedrunner");

    private final String description;

    WinReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
