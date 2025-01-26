package ru.nightmirror.wlbytime.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;

@UtilityClass
public final class ColorsUtils {
    private static final Map<String, String> COLOR_MAPPINGS = Map.ofEntries(
            Map.entry("&0", "<black>"), Map.entry("§0", "<black>"),
            Map.entry("&1", "<dark_blue>"), Map.entry("§1", "<dark_blue>"),
            Map.entry("&2", "<dark_green>"), Map.entry("§2", "<dark_green>"),
            Map.entry("&3", "<dark_aqua>"), Map.entry("§3", "<dark_aqua>"),
            Map.entry("&4", "<dark_red>"), Map.entry("§4", "<dark_red>"),
            Map.entry("&5", "<dark_purple>"), Map.entry("§5", "<dark_purple>"),
            Map.entry("&6", "<gold>"), Map.entry("§6", "<gold>"),
            Map.entry("&7", "<grey>"), Map.entry("§7", "<grey>"),
            Map.entry("&8", "<dark_grey>"), Map.entry("§8", "<dark_grey>"),
            Map.entry("&9", "<blue>"), Map.entry("§9", "<blue>"),
            Map.entry("&a", "<green>"), Map.entry("§a", "<green>"),
            Map.entry("&b", "<aqua>"), Map.entry("§b", "<aqua>"),
            Map.entry("&c", "<red>"), Map.entry("§c", "<red>"),
            Map.entry("&d", "<light_purple>"), Map.entry("§d", "<light_purple>"),
            Map.entry("&e", "<yellow>"), Map.entry("§e", "<yellow>"),
            Map.entry("&f", "<white>"), Map.entry("§f", "<white>")
    );

    public static List<Component> convert(List<String> messages) {
        return messages.stream().map(ColorsUtils::convertMessage).toList();
    }

    public static Component convertMessage(String message) {
        return MiniMessage.miniMessage().deserialize(checkAndReplaceLegacyColors(message));
    }

    private static String checkAndReplaceLegacyColors(String message) {
        message = message.replace("&", "§");
        if (message.contains("&") || message.contains("§")) {
            return replaceLegacyColors(message);
        }
        return message;
    }

    private static String replaceLegacyColors(String text) {
        for (Map.Entry<String, String> entry : COLOR_MAPPINGS.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }
}
