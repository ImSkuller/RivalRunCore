package xyz.skuller.rivalRun.managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import xyz.skuller.rivalRun.RivalRun;

import java.util.List;

public class ConfigManager {

    public void ConfigUpdate(Double latest, Plugin plugin) {

        var instance = RivalRun.getInstance();

        // Header Logic
        instance.getConfig().options().setHeader(List.of(
                "############################################################",
                "# +------------------------------------------------------+ #",
                "# |                 Plugin By Skuller                    | #",
                "# |            https://discord.gg/58QeTrs3n9             | #" ,
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
        ));

        double ConfigVersion = instance.getConfig().getDouble("version", 0.0);

        if (ConfigVersion != latest) {
            Bukkit.getConsoleSender().sendRichMessage("<red>[Rival Run] Config Version Outdated");
            Bukkit.getConsoleSender().sendRichMessage("<red>[Rival Run] Updating Config to Version: " + latest);

            plugin.saveResource("config.yml", true);
            instance.reloadConfig();

            instance.getConfig().set("version", latest);
            instance.saveConfig();

            Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Config Version Updated Successfully!");
        }
        else {
            Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Config Version up to date (" + ConfigVersion + ")");
            Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Config Files Loaded Successfully!");
        }

        instance.saveConfig();

    }

}

