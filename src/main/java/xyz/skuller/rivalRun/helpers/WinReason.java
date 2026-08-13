package xyz.skuller.rivalRun.helpers;

public enum WinReason {

    DRAGON("Defeated the Ender Dragon"),
    ELIMINATION("Last team standing");

    private final String description;

    WinReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
