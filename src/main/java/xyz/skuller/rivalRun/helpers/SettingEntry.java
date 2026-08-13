package xyz.skuller.rivalRun.helpers;

import org.bukkit.Material;

// One editable line in a settings category menu. TOGGLE flips a boolean in
// place; CYCLE advances through a fixed list of string options; TEXT opens a
// chat prompt for a free-form string/number value.
public record SettingEntry(
        Material icon,
        String label,
        String description,
        SettingType type,
        String configPath,
        String[] cycleOptions
) {

    public enum SettingType {
        TOGGLE,
        CYCLE,
        TEXT
    }

    public static SettingEntry toggle(Material icon, String label, String description, String configPath) {
        return new SettingEntry(icon, label, description, SettingType.TOGGLE, configPath, null);
    }

    public static SettingEntry text(Material icon, String label, String description, String configPath) {
        return new SettingEntry(icon, label, description, SettingType.TEXT, configPath, null);
    }

    public static SettingEntry cycle(Material icon, String label, String description, String configPath, String... options) {
        return new SettingEntry(icon, label, description, SettingType.CYCLE, configPath, options);
    }

}
