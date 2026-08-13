package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.commands.GameCommands;
import xyz.skuller.rivalRun.helpers.GuideBook;
import xyz.skuller.rivalRun.helpers.SimpleMenu;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

import java.util.List;

// A control panel for admins so they don't have to memorize /rivalrun
// subcommands. Opened by running /rivalrun with no arguments.
public class AdminMenu extends SimpleMenu {

    public AdminMenu() {
        super(Rows.TWO, "§4Rival Run §0| §8Admin Menu");
    }

    @Override
    public void onSetItems() {
        clearMenu();

        GameCommands gc = RivalRun.getInstance().getGameCommands();
        GameStateManager gsm = RivalRun.getInstance().getGameStateManager();
        TeamsManager tm = RivalRun.getInstance().getTeamManager();

        setButton(0, Material.COMMAND_BLOCK, "Settings", NamedTextColor.AQUA,
                "Edit every config value in-game.", player -> new SettingsMenu().open(player));

        setButton(1, Material.LIME_DYE, "Start Game", NamedTextColor.GREEN,
                "Begins the countdown for a new run.", player -> {
                    gc.startGame();
                    player.closeInventory();
                });

        boolean paused = gsm.isState(GameStateManager.GameStates.PAUSED);
        setButton(2, paused ? Material.LIME_DYE : Material.YELLOW_DYE,
                paused ? "Resume Game" : "Pause Game", NamedTextColor.YELLOW,
                paused ? "Unfreezes the current run." : "Freezes the current run.", player -> {
                    if (paused) gc.resumeGame(); else gc.pauseGame(player);
                    open(player);
                });

        setButton(3, Material.GOLDEN_APPLE, "Player Buffs", NamedTextColor.AQUA,
                "Set per-player handicaps before the game starts.", player -> new BuffPlayerListMenu().open(player));

        setButton(4, Material.RED_DYE, "End Game", NamedTextColor.RED,
                "Force ends the run without a winner.", player -> {
                    gc.endGame();
                    player.closeInventory();
                });

        setButton(5, Material.BARRIER, "Reset Game", NamedTextColor.RED,
                "Resets teams and game state entirely.", player -> {
                    gc.resetGame();
                    player.sendRichMessage("<green>The game has been reset.");
                    player.closeInventory();
                });

        setButton(6, Material.TNT, "Reset World", NamedTextColor.RED,
                "Regenerates the entire world with a new seed.", player -> new WorldResetConfirmMenu().open(player));

        setButton(7, Material.OAK_DOOR, "Lock Teams", NamedTextColor.GOLD,
                "Stops anyone joining/switching teams.", player -> {
                    tm.lockTeams();
                    player.sendRichMessage("<red>Nobody can switch teams or join a team anymore.");
                    open(player);
                });

        setButton(8, Material.OAK_DOOR, "Unlock Teams", NamedTextColor.GOLD,
                "Allows joining/switching teams again.", player -> {
                    tm.unlockTeams();
                    player.sendRichMessage("<red>Anyone can switch teams or join teams freely now.");
                    open(player);
                });

        setButton(13, Material.WRITTEN_BOOK, "Help", NamedTextColor.YELLOW,
                "Open the admin guide book.", GuideBook::openAdminGuide);
    }

    private void setButton(int slot, Material material, String name, NamedTextColor color, String description, java.util.function.Consumer<org.bukkit.entity.Player> action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text(description, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);

        setItem(slot, item, player -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            action.accept(player);
        });
    }
}
