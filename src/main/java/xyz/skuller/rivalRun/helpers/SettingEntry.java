package xyz.skuller.rivalRun.helpers;

import org.bukkit.Material;

// One editable line in a settings category menu. TOGGLE flips a boolean in
// place; CYCLE advances through a fixed list of string options (stored as
// the option's index, defaulting to defaultCycleIndex when unset); TEXT
// opens a chat prompt for a free-form string; NUMBER opens a chat prompt
// that's always parsed as an integer, regardless of whether the config
// path has ever been set before.
public record SettingEntry(
        Material icon,
        String label,
        String description,
        SettingType type,
        String configPath,
        String[] cycleOptions,
        int defaultCycleIndex
) {

    public enum SettingType {
        TOGGLE,
        CYCLE,
        TEXT,
        NUMBER
    }

    public static SettingEntry toggle(Material icon, String label, String description, String configPath) {
        return new SettingEntry(icon, label, description, SettingType.TOGGLE, configPath, null, 0);
    }

    public static SettingEntry text(Material icon, String label, String description, String configPath) {
        return new SettingEntry(icon, label, description, SettingType.TEXT, configPath, null, 0);
    }

    public static SettingEntry number(Material icon, String label, String description, String configPath) {
        return new SettingEntry(icon, label, description, SettingType.NUMBER, configPath, null, 0);
    }

    public static SettingEntry cycle(Material icon, String label, String description, String configPath, int defaultIndex, String... options) {
        return new SettingEntry(icon, label, description, SettingType.CYCLE, configPath, options, defaultIndex);
    }

}
