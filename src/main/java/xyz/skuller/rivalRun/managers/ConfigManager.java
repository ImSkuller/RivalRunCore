package xyz.skuller.rivalRun.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.skuller.rivalRun.RivalRun;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConfigManager {

    private static final List<String> HEADER = List.of(
            "############################################################",
            "# +------------------------------------------------------+ #",
            "# |                 Plugin By Skuller                    | #",
            "# |            https://discord.gg/58QeTrs3n9             | #",
            "# +------------------------------------------------------+ #",
            "############################################################",
            " ",
            "# If you receive an error when Rival Run loads,  ensure that:",
            "#  - No tabs are present: YAML only allows spaces",
            "#  - Indents are correct: YAML hierarchy is based entirely on indentation",
            " ",
            "#<-------------------------------------------------------->#",
            "# Configuration File Starts Here",
            "#<-------------------------------------------------------->#",
            " "
    );

    // Non-destructively brings the on-disk config up to date: creates it if missing,
    // then adds any keys that are new since the user's version without touching
    // anything they've already customized.
    public void update(RivalRun plugin, double latest) {

        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration live = plugin.getConfig();
        FileConfiguration bundled = loadBundledDefaults(plugin);

        if (bundled == null) {
            Bukkit.getConsoleSender().sendRichMessage("<red>[Rival Run] Could not read the bundled default config.yml, skipping migration.");
            return;
        }

        double liveVersion = live.getDouble("version", 0.0);

        boolean changed = mergeMissingKeys(bundled, live);

        if (liveVersion != latest) {
            live.set("version", latest);
            changed = true;
            Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Config updated from version <gold>" + liveVersion + "<green> to <gold>" + latest + "<green> (existing settings were preserved)");
        }

        if (changed) {
            live.options().setHeader(HEADER);
            plugin.saveConfig();
        } else {
            Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Config up to date (" + liveVersion + ")");
        }
    }

    private FileConfiguration loadBundledDefaults(RivalRun plugin) {
        try (InputStream in = plugin.getResource("config.yml")) {
            if (in == null) return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return null;
        }
    }

    // Copies over any key present in the bundled defaults but missing from the live
    // config. Returns true if anything was added.
    private boolean mergeMissingKeys(ConfigurationSection defaults, ConfigurationSection live) {
        boolean changed = false;

        for (String key : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(key)) continue;

            if (!live.isSet(key)) {
                live.set(key, defaults.get(key));
                changed = true;
            }
        }

        return changed;
    }

}
