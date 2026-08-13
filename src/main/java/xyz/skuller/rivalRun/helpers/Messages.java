package xyz.skuller.rivalRun.helpers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import xyz.skuller.rivalRun.RivalRun;

// Small helper for the config's messages.* MiniMessage templates, with
// {placeholder} substitution done before deserializing.
public class Messages {

    public static Component get(String path, String... placeholders) {
        String raw = RivalRun.getInstance().getConfig().getString(path, "");

        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            raw = raw.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }

        return MiniMessage.miniMessage().deserialize(raw);
    }

}
