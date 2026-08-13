package xyz.skuller.rivalRun.helpers;

import net.kyori.adventure.text.format.NamedTextColor;

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
}