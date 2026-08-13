package xyz.skuller.rivalRun.helpers;

import net.kyori.adventure.text.format.NamedTextColor;

// Team-speedrunning route milestones. Each is awarded once, globally, to
// whichever team's member gets there first.
public enum AchievementType {

    NETHER("Into the Nether", NamedTextColor.RED),
    BLAZE_ROD("Blaze Rod", NamedTextColor.GOLD),
    ENDER_PEARL("Ender Pearl", NamedTextColor.GREEN),
    EYE_OF_ENDER("Eye of Ender", NamedTextColor.LIGHT_PURPLE),
    ENTER_END("The End", NamedTextColor.DARK_PURPLE),
    DAMAGE_DRAGON("Dragon's Bane", NamedTextColor.DARK_RED);

    private final String displayName;
    private final NamedTextColor color;

    AchievementType(String displayName, NamedTextColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NamedTextColor getColor() {
        return color;
    }

}
