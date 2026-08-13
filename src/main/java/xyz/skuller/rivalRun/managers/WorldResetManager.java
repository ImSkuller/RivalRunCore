package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import xyz.skuller.rivalRun.RivalRun;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

// Regenerates a fresh Overworld/Nether/End with a brand new seed - a true
// clean slate, rather than the lightweight team/state-only reset in
// GameCommands. Only ever triggered after WorldResetConfirmMenu's confirm
// click, since this can permanently delete world data.
//
// RivalRun deliberately never plays in the server's *default* world
// (server.properties' level-name) - Bukkit refuses to unload that world
// via plugin API, full stop, which would make it impossible to regenerate
// live. Instead this manages its own dedicated named world set from first
// launch on, tracked as worldReset.currentWorld in config.yml, which is
// free to unload/delete/recreate at will using plain, stable Bukkit API.
public class WorldResetManager {

    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm");
    private static final String CURRENT_WORLD_PATH = "worldReset.currentWorld";

    // Called once from onEnable(). Creates the initial dedicated world set
    // on first ever run, otherwise just re-loads the recorded one (a safe
    // no-op if those folders already exist on disk).
    public void ensureWorldsLoaded() {
        RivalRun plugin = RivalRun.getInstance();
        String baseName = plugin.getConfig().getString(CURRENT_WORLD_PATH, "");

        if (baseName.isEmpty()) {
            baseName = "rivalrun_" + System.currentTimeMillis();
            createWorldSet(baseName, new Random().nextLong());
            plugin.getConfig().set(CURRENT_WORLD_PATH, baseName);
            plugin.saveConfig();
            Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Created the initial game world: <gold>" + baseName);
        } else {
            createWorldSet(baseName, 0L);
            Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] Loaded game world: <gold>" + baseName);
        }
    }

    // The Overworld players should be in while playing RivalRun.
    public World getCurrentOverworld() {
        String baseName = RivalRun.getInstance().getConfig().getString(CURRENT_WORLD_PATH, "");
        return baseName.isEmpty() ? null : Bukkit.getWorld(baseName);
    }

    public void resetWorlds() {
        RivalRun plugin = RivalRun.getInstance();
        String oldBaseName = plugin.getConfig().getString(CURRENT_WORLD_PATH, "");

        Bukkit.broadcast(Component.text("Generating a fresh world with a new seed - the server may lag briefly...", NamedTextColor.YELLOW));
        Bukkit.getConsoleSender().sendRichMessage("<yellow>[Rival Run] World reset started.");

        String newBaseName = "rivalrun_" + System.currentTimeMillis();
        World newOverworld = createWorldSet(newBaseName, new Random().nextLong());

        // If the new world didn't come up, stop here - do NOT touch the old
        // one. Players are still standing in it; removing it now with
        // nowhere valid to put them would be far worse than a failed reset.
        if (newOverworld == null) {
            Bukkit.broadcast(Component.text("World reset failed - could not create the new world. Nothing was changed.", NamedTextColor.RED));
            Bukkit.getConsoleSender().sendRichMessage("<red>[Rival Run] World reset failed: could not create the new Overworld.");
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(newOverworld.getSpawnLocation());
        }

        if (!oldBaseName.isEmpty()) {
            removeOldWorld(Bukkit.getWorld(oldBaseName));
            removeOldWorld(Bukkit.getWorld(oldBaseName + "_nether"));
            removeOldWorld(Bukkit.getWorld(oldBaseName + "_the_end"));
        }

        plugin.getConfig().set(CURRENT_WORLD_PATH, newBaseName);
        plugin.saveConfig();

        plugin.getGameCommands().resetGame();

        Bukkit.broadcast(Component.text("A brand new world has been generated. Fresh start!", NamedTextColor.GREEN));
        Bukkit.getConsoleSender().sendRichMessage("<green>[Rival Run] World reset complete: <gold>" + newBaseName);
    }

    // Creates (or, if the folders already exist, simply loads) the three
    // dimensions of a world set sharing one base name and seed. Returns the
    // Overworld, or null if it failed to create.
    private World createWorldSet(String baseName, long seed) {
        try {
            World overworld = new WorldCreator(baseName).environment(World.Environment.NORMAL).seed(seed).createWorld();
            new WorldCreator(baseName + "_nether").environment(World.Environment.NETHER).seed(seed).createWorld();
            new WorldCreator(baseName + "_the_end").environment(World.Environment.THE_END).seed(seed).createWorld();
            return overworld;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendRichMessage("<red>[Rival Run] Failed to create world set '" + baseName + "': " + e.getMessage());
            return null;
        }
    }

    private void removeOldWorld(World world) {
        if (world == null) return;

        File folder = world.getWorldFolder();
        Bukkit.unloadWorld(world, false);

        if (!isInsideWorldContainer(folder)) {
            Bukkit.getConsoleSender().sendRichMessage("<red>[Rival Run] Refused to remove " + folder + " - it isn't inside the world container.");
            return;
        }

        if (RivalRun.getInstance().getConfig().getBoolean("worldReset.deleteOldWorlds", true)) {
            deleteRecursively(folder);
        } else {
            archive(folder);
        }
    }

    private boolean isInsideWorldContainer(File folder) {
        try {
            Path container = Bukkit.getWorldContainer().getCanonicalFile().toPath();
            Path target = folder.getCanonicalFile().toPath();
            return target.startsWith(container);
        } catch (Exception e) {
            return false;
        }
    }

    private void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }

    private void archive(File folder) {
        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP);
        File target = new File(folder.getParentFile(), folder.getName() + "_backup_" + timestamp);

        if (!folder.renameTo(target)) {
            Bukkit.getConsoleSender().sendRichMessage("<red>[Rival Run] Could not archive " + folder.getName() + " - it was unloaded but left in place.");
        }
    }

}
