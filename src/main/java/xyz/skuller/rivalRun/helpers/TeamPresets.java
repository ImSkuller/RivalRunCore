package xyz.skuller.rivalRun.helpers;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;

public class TeamPresets {

    public enum TeamPreset {

        DEFAULT(new String[]{"RED", "BLUE", "GREEN", "YELLOW", "AQUA", "PURPLE", "WHITE"}),
        PASSIVE(new String[]{"Pig", "Cow", "Chicken", "Sheep", "Squid", "Axolotl", "Rabbit"}),
        HOSTILE(new String[]{"Zombie", "Creeper", "Blaze", "Piglin", "Enderman", "Warden", "Ghast"}),
        FLOWERS(new String[]{"Rose", "Dandelions", "Sunflower", "Cornflower", "Tulip", "Lilly", "Poppy"}),
        BLOCKS(new String[]{"Wood", "Stone", "Sand", "Deepslate", "Obsidian", "Endstone", "Prismarine"});

        private final String[] names;

        TeamPreset(String[] names) {
            this.names = names;
        }

        public String[] getNames() {
            return names;
        }
    }

    public static NamedTextColor getColorByIndex(int index) {
        return switch (index) {
            case 0 -> NamedTextColor.RED;
            case 1 -> NamedTextColor.BLUE;
            case 2 -> NamedTextColor.GREEN;
            case 3 -> NamedTextColor.YELLOW;
            case 4 -> NamedTextColor.AQUA;
            case 5 -> NamedTextColor.LIGHT_PURPLE;
            case 6 -> NamedTextColor.WHITE;
            default -> NamedTextColor.GRAY;
        };
    }

    // Colored wool matching a team's NamedTextColor, for GUI icons.
    public static Material getWoolByColor(NamedTextColor color) {
        if (color == NamedTextColor.RED) return Material.RED_WOOL;
        if (color == NamedTextColor.BLUE) return Material.BLUE_WOOL;
        if (color == NamedTextColor.GREEN) return Material.GREEN_WOOL;
        if (color == NamedTextColor.YELLOW) return Material.YELLOW_WOOL;
        if (color == NamedTextColor.AQUA) return Material.LIGHT_BLUE_WOOL;
        if (color == NamedTextColor.LIGHT_PURPLE) return Material.MAGENTA_WOOL;
        if (color == NamedTextColor.WHITE) return Material.WHITE_WOOL;
        return Material.GRAY_WOOL;
    }

    // Bukkit firework Color matching a team's NamedTextColor, for the win celebration.
    public static Color getFireworkColorByColor(NamedTextColor color) {
        if (color == NamedTextColor.RED) return Color.RED;
        if (color == NamedTextColor.BLUE) return Color.BLUE;
        if (color == NamedTextColor.GREEN) return Color.LIME;
        if (color == NamedTextColor.YELLOW) return Color.YELLOW;
        if (color == NamedTextColor.AQUA) return Color.AQUA;
        if (color == NamedTextColor.LIGHT_PURPLE) return Color.FUCHSIA;
        if (color == NamedTextColor.WHITE) return Color.WHITE;
        return Color.SILVER;
    }
}