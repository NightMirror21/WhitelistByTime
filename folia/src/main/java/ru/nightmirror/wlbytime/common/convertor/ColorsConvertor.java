package ru.nightmirror.wlbytime.common.convertor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import ru.nightmirror.wlbytime.WhitelistByTime;

import java.util.List;

public class ColorsConvertor {

    public static List<Component> convert(List<String> list) {
        return list.stream().map(ColorsConvertor::convert).toList();
    }

    public static Component convert(String message) {
        return MiniMessage.miniMessage().deserialize(checkLegacy(message));
    }

    public static String checkLegacy(String message) {
        message = message.replaceAll("&", "§");

        if (message.contains("&") || message.contains("§")) {
            WhitelistByTime.error("Remove legacy color(s) (starts with '&' or '§') from config!");
            WhitelistByTime.error("Paper and paper family don't support legacy color formats");
            WhitelistByTime.error("Use MiniMessage by kyori and make life easier!");
            WhitelistByTime.error("https://docs.advntr.dev/minimessage/format.html");
            message = replaceLegacyColors(message);
        }

        return message;
    }

    private static String replaceLegacyColors(String text) {
        return text
                .replaceAll("&0", "<black>").replaceAll("§0", "<black>")
                .replaceAll("&1", "<dark_blue>").replaceAll("§1", "<dark_blue>")
                .replaceAll("&2", "<dark_green>").replaceAll("§2", "<dark_green>")
                .replaceAll("&3", "<dark_aqua>").replaceAll("§3", "<dark_aqua>")
                .replaceAll("&4", "<dark_red>").replaceAll("§4", "<dark_red>")
                .replaceAll("&5", "<dark_purple>").replaceAll("§5", "<dark_purple>")
                .replaceAll("&6", "<gold>").replaceAll("§6", "<gold>")
                .replaceAll("&7", "<grey>").replaceAll("§7", "<grey>")
                .replaceAll("&8", "<dark_grey>").replaceAll("§8", "<dark_grey>")
                .replaceAll("&9", "<blue>").replaceAll("§9", "<blue>")
                .replaceAll("&a", "<green>").replaceAll("§a", "<green>")
                .replaceAll("&b", "<aqua>").replaceAll("§b", "<aqua>")
                .replaceAll("&c", "<red>").replaceAll("§c", "<red>")
                .replaceAll("&d", "<light_purple>").replaceAll("§d", "<light_purple>")
                .replaceAll("&e", "<yellow>").replaceAll("§e", "<yellow>")
                .replaceAll("&f", "<white>").replaceAll("§f", "<white>");
    }
}
