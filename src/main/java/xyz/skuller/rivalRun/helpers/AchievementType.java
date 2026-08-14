package xyz.skuller.rivalRun.helpers;

import net.kyori.adventure.text.format.NamedTextColor;

// Every achievement, tagged with which gamemode it belongs to so
// AchievementManager/the listeners can filter by the active mode. Each is
// awarded once, globally, to whichever team's member gets there first
// (see AchievementManager).
public enum AchievementType {

    // ---- Classic: the original 6 route milestones ----
    NETHER("Into the Nether", NamedTextColor.RED, Gamemode.CLASSIC),
    BLAZE_ROD("Blaze Rod", NamedTextColor.GOLD, Gamemode.CLASSIC),
    ENDER_PEARL("Ender Pearl", NamedTextColor.GREEN, Gamemode.CLASSIC),
    EYE_OF_ENDER("Eye of Ender", NamedTextColor.LIGHT_PURPLE, Gamemode.CLASSIC),
    ENTER_END("The End", NamedTextColor.DARK_PURPLE, Gamemode.CLASSIC),
    DAMAGE_DRAGON("Dragon's Bane", NamedTextColor.DARK_RED, Gamemode.CLASSIC),

    // ---- Classic: 24 more ----
    DIAMONDS("Diamonds!", NamedTextColor.AQUA, Gamemode.CLASSIC),
    IRON_PICKAXE("Getting an Upgrade", NamedTextColor.WHITE, Gamemode.CLASSIC),
    DIAMOND_PICKAXE("Acquire Hardware", NamedTextColor.AQUA, Gamemode.CLASSIC),
    BLACKSMITH("Blacksmith", NamedTextColor.AQUA, Gamemode.CLASSIC),
    FULL_DIAMOND_ARMOR("Suit Up", NamedTextColor.AQUA, Gamemode.CLASSIC),
    FULL_IRON_ARMOR("Iron Man", NamedTextColor.WHITE, Gamemode.CLASSIC),
    OBSIDIAN_COLLECTED("Hot Stuff", NamedTextColor.DARK_PURPLE, Gamemode.CLASSIC),
    NETHER_WART_COLLECTED("Wart Hunter", NamedTextColor.RED, Gamemode.CLASSIC),
    NETHER_STAR_COLLECTED("Bring Home the Beacon", NamedTextColor.YELLOW, Gamemode.CLASSIC),
    ENCHANTED_ITEM("Enchanter", NamedTextColor.LIGHT_PURPLE, Gamemode.CLASSIC),
    GLASS_BOTTLE_COLLECTED("Bottled Up", NamedTextColor.WHITE, Gamemode.CLASSIC),
    POTION_CONSUMED("A Furious Cocktail", NamedTextColor.LIGHT_PURPLE, Gamemode.CLASSIC),
    NETHERITE_SCRAP_COLLECTED("Cover Me in Debris", NamedTextColor.DARK_GRAY, Gamemode.CLASSIC),
    NETHERITE_INGOT_COLLECTED("Hidden in the Depths", NamedTextColor.DARK_GRAY, Gamemode.CLASSIC),
    ZOMBIE_CURE_STARTED("Zombie Doctor", NamedTextColor.GREEN, Gamemode.CLASSIC),
    SNIPER_DUEL("Sniper Duel", NamedTextColor.GRAY, Gamemode.CLASSIC),
    MONSTER_HUNTER("Monster Hunter", NamedTextColor.RED, Gamemode.CLASSIC),
    MASTER_ANGLER("Master Angler", NamedTextColor.AQUA, Gamemode.CLASSIC),
    IRON_BELLY("Iron Belly", NamedTextColor.GREEN, Gamemode.CLASSIC),
    SKY_HIGH("Sky's the Limit", NamedTextColor.AQUA, Gamemode.CLASSIC),
    FREE_FALLER("Free Faller", NamedTextColor.WHITE, Gamemode.CLASSIC),
    GREEN_THUMB("Green Thumb", NamedTextColor.GREEN, Gamemode.CLASSIC),
    ENDER_BUSINESS("Ender Business", NamedTextColor.DARK_PURPLE, Gamemode.CLASSIC),
    FLAWLESS_VICTORY("Flawless Victory", NamedTextColor.GOLD, Gamemode.CLASSIC),

    // ---- Manhunt: Hunters ----
    FIRST_BLOOD("First Blood", NamedTextColor.RED, Gamemode.MANHUNT),
    THE_HUNT_IS_OVER("The Hunt Is Over", NamedTextColor.RED, Gamemode.MANHUNT),
    TEAM_WIPE("Team Wipe", NamedTextColor.RED, Gamemode.MANHUNT),
    QUICK_HUNT("Quick Hunt", NamedTextColor.GOLD, Gamemode.MANHUNT),
    CLEAN_SWEEP("Clean Sweep", NamedTextColor.RED, Gamemode.MANHUNT),
    AMBUSH("Ambush", NamedTextColor.DARK_RED, Gamemode.MANHUNT),
    NO_MERCY("No Mercy", NamedTextColor.DARK_RED, Gamemode.MANHUNT),
    MARKSMAN("Marksman", NamedTextColor.GRAY, Gamemode.MANHUNT),
    FIRST_STRIKE("First Strike", NamedTextColor.RED, Gamemode.MANHUNT),
    FAST_START("Fast Start", NamedTextColor.YELLOW, Gamemode.MANHUNT),

    // ---- Manhunt: Speedrunners ----
    NEVER_SAY_DIE("Never Say Die", NamedTextColor.AQUA, Gamemode.MANHUNT),
    SPEED_DEMON("Speed Demon", NamedTextColor.GOLD, Gamemode.MANHUNT),
    UNDERDOG_VICTORY("Underdog Victory", NamedTextColor.AQUA, Gamemode.MANHUNT),
    FULL_SQUAD("Full Squad", NamedTextColor.GREEN, Gamemode.MANHUNT),
    SACRIFICE_PLAY("Sacrifice Play", NamedTextColor.GRAY, Gamemode.MANHUNT),
    COMEBACK_KID("Comeback Kid", NamedTextColor.GREEN, Gamemode.MANHUNT),
    LAST_ONE_STANDING("Last One Standing", NamedTextColor.AQUA, Gamemode.MANHUNT),
    AGAINST_ALL_ODDS("Against All Odds", NamedTextColor.GOLD, Gamemode.MANHUNT),
    EVASIVE_MANEUVERS("Evasive Maneuvers", NamedTextColor.LIGHT_PURPLE, Gamemode.MANHUNT),
    GREAT_ESCAPE("Great Escape", NamedTextColor.AQUA, Gamemode.MANHUNT),

    // ---- Manhunt: Shared ----
    REVIVAL("Revival", NamedTextColor.GREEN, Gamemode.MANHUNT),
    ONE_SHOT("One Shot", NamedTextColor.DARK_RED, Gamemode.MANHUNT),
    MARATHON("Marathon", NamedTextColor.GRAY, Gamemode.MANHUNT),
    BLOOD_FEUD("Blood Feud", NamedTextColor.RED, Gamemode.MANHUNT),
    IRON_CURTAIN("Iron Curtain", NamedTextColor.RED, Gamemode.MANHUNT),
    PERFECT_HUNT("Perfect Hunt", NamedTextColor.RED, Gamemode.MANHUNT),
    PREPARED("Prepared", NamedTextColor.AQUA, Gamemode.MANHUNT),
    HOUSEWARMING("Housewarming", NamedTextColor.YELLOW, Gamemode.MANHUNT),
    SHARP_EYE("Sharp Eye", NamedTextColor.RED, Gamemode.MANHUNT),
    IRON_GRIP("Iron Grip", NamedTextColor.DARK_RED, Gamemode.MANHUNT);

    private final String displayName;
    private final NamedTextColor color;
    private final Gamemode gamemode;

    AchievementType(String displayName, NamedTextColor color, Gamemode gamemode) {
        this.displayName = displayName;
        this.color = color;
        this.gamemode = gamemode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public Gamemode getGamemode() {
        return gamemode;
    }

}
