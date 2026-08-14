package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.commands.GameCommands;
import xyz.skuller.rivalRun.helpers.GuideBook;
import xyz.skuller.rivalRun.helpers.SimpleMenu;
import xyz.skuller.rivalRun.managers.GameStateManager;
import xyz.skuller.rivalRun.managers.TeamsManager;

import java.util.List;
import java.util.function.Consumer;

// A control panel for admins so they don't have to memorize /rivalrun
// subcommands. Opened by running /rivalrun with no arguments.
public class AdminMenu extends SimpleMenu {

    public AdminMenu() {
        super(Rows.THREE, "§4Rival Run §0| §8Admin Menu");
    }

    @Override
    public void onSetItems() {
        clearMenu();

        fillBorder();

        GameCommands gc = RivalRun.getInstance().getGameCommands();
        GameStateManager gsm = RivalRun.getInstance().getGameStateManager();
        TeamsManager tm = RivalRun.getInstance().getTeamManager();

        setButton(1, Material.COMMAND_BLOCK, "Settings", NamedTextColor.AQUA,
                "Edit every config value in-game.", player -> new SettingsMenu(() -> new AdminMenu().open(player)).open(player));

        setButton(2, Material.COMPASS, "Gamemode", NamedTextColor.LIGHT_PURPLE,
                "Currently: " + RivalRun.getInstance().getGamemodeManager().getActive().name() + ".", player -> new GamemodePickerMenu(() -> new AdminMenu().open(player)).open(player));

        setButton(3, Material.GOLDEN_APPLE, "Player Buffs", NamedTextColor.AQUA,
                "Set per-player handicaps before the game starts.", player -> new BuffPlayerListMenu(() -> new AdminMenu().open(player)).open(player));

        boolean teamsLocked = tm.isTeamsLocked();
        setButton(4, Material.OAK_DOOR, teamsLocked ? "Unlock Teams" : "Lock Teams", NamedTextColor.GOLD,
                teamsLocked ? "Allows joining/switching teams again." : "Stops anyone joining/switching teams.", player -> {
                    if (teamsLocked) tm.unlockTeams(); else tm.lockTeams();
                    player.sendRichMessage(teamsLocked
                            ? "<red>Anyone can switch teams or join teams freely now."
                            : "<red>Nobody can switch teams or join a team anymore.");
                    open(player);
                });

        setButton(5, Material.NETHER_STAR, "Match Summary", NamedTextColor.GOLD,
                "See the last completed match's results.", player -> new MatchSummaryMenu(() -> new AdminMenu().open(player)).open(player));

        setButton(6, Material.WRITTEN_BOOK, "Help", NamedTextColor.YELLOW,
                "Open the admin guide book.", GuideBook::openAdminGuide);

        setButton(10, Material.LIME_DYE, "Start Game", NamedTextColor.GREEN,
                "Begins the countdown for a new run.", player -> {
                    gc.startGame();
                    player.closeInventory();
                });

        boolean paused = gsm.isState(GameStateManager.GameStates.PAUSED);
        setButton(11, paused ? Material.LIME_DYE : Material.YELLOW_DYE,
                paused ? "Resume Game" : "Pause Game", NamedTextColor.YELLOW,
                paused ? "Unfreezes the current run." : "Freezes the current run.", player -> {
                    if (paused) gc.resumeGame(); else gc.pauseGame(player);
                    open(player);
                });

        setButton(12, Material.RED_DYE, "End Game", NamedTextColor.RED,
                "Force ends the run without a winner.", player -> {
                    gc.endGame();
                    player.closeInventory();
                });

        setButton(13, Material.BARRIER, "Reset Game", NamedTextColor.RED,
                "Resets teams, state, and every player back to a fresh start.", player -> {
                    gc.resetGame();
                    player.sendRichMessage("<green>The game has been reset.");
                    player.closeInventory();
                });

        setButton(14, Material.TNT, "Reset World", NamedTextColor.RED,
                "Regenerates the entire world with a new seed.", player -> new WorldResetConfirmMenu(() -> new AdminMenu().open(player)).open(player));
    }

    // A plain glass border around the buttons instead of a bare grid of
    // gaps - every slot not otherwise set gets a filler pane.
    private void fillBorder() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        pane.setItemMeta(meta);

        for (int slot = 0; slot < getInventory().getSize(); slot++) {
            setItem(slot, pane);
        }
    }

    private void setButton(int slot, Material material, String name, NamedTextColor color, String description, Consumer<Player> action) {
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
