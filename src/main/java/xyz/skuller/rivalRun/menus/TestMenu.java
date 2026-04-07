package xyz.skuller.rivalRun.menus;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.skuller.rivalRun.RivalRun;
import xyz.skuller.rivalRun.helpers.SimpleMenu;

public class TestMenu extends SimpleMenu {

    boolean enabled = RivalRun.getInstance().getConfig().getBoolean("gui.test-gui");
    boolean fillers = RivalRun.getInstance().getConfig().getBoolean("gui.filler");
    String itemName1 = RivalRun.getInstance().getConfig().getString("gui.filler-1-name");
    String itemName2 = RivalRun.getInstance().getConfig().getString("gui.filler-2-name");



    public TestMenu() {
        super(Rows.THREE, "§4Rival Run §0| §8Team Select");
    }

    @Override
    public void onSetItems() {
        final ItemStack filler_1 = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        final ItemMeta filler_1Meta = filler_1.getItemMeta();
        filler_1Meta.displayName(MiniMessage.miniMessage().deserialize(itemName1));
        filler_1.setItemMeta(filler_1Meta);


        final ItemStack filler_2 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        final ItemMeta filler_2Meta = filler_2.getItemMeta();
        filler_2Meta.displayName(MiniMessage.miniMessage().deserialize(itemName2));
        filler_2.setItemMeta(filler_2Meta);

        // Setting the fillers
        if (fillers) {
            setItem(0, filler_1);
            setItem(1, filler_1);
            setItem(2, filler_1);
            setItem(3, filler_2);
            setItem(4, filler_2);
            setItem(5, filler_2);
            setItem(6, filler_1);
            setItem(7, filler_2);
            setItem(8, filler_2);
            setItem(9, filler_2);
            setItem(17, filler_2);
            setItem(18, filler_2);
            setItem(19, filler_2);
            setItem(20, filler_1);
            setItem(21, filler_2);
            setItem(22, filler_2);
            setItem(23, filler_2);
            setItem(24, filler_1);
            setItem(25, filler_1);
            setItem(26, filler_1);
        }
    }
}
