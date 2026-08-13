package xyz.skuller.rivalRun.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

// Small reusable "type your answer in chat" framework, used by the in-game
// settings editor and the custom team creator. Only one pending prompt per
// player at a time.
public class ChatInputManager {

    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    public void request(Player player, Component prompt, Consumer<String> onSubmit) {
        pending.put(player.getUniqueId(), onSubmit);
        player.closeInventory();
        player.sendMessage(prompt);
        player.sendMessage(Component.text("Type your answer in chat, or type 'cancel' to abort.", NamedTextColor.GRAY));
    }

    public boolean isAwaitingInput(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    public void handle(Player player, String message) {
        Consumer<String> callback = pending.remove(player.getUniqueId());
        if (callback == null) return;

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(Component.text("Cancelled.", NamedTextColor.RED));
            return;
        }

        callback.accept(message);
    }

    public void cancel(Player player) {
        pending.remove(player.getUniqueId());
    }

}
