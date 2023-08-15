package ru.nightmirror.wlbytime.common.convertor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class ColorsConvertor {

    public static List<Component> convert(List<String> list) {
        return list.stream().map(ColorsConvertor::convert).toList();
    }

    public static Component convert(String message) {
        return MiniMessage.miniMessage().deserialize(convertHexAndLegacy(message));
    }

    public static String convertHexAndLegacy(String message) {
        message = message.replaceAll("&", "§");

        StringBuilder replaced = new StringBuilder();
        char[] charOfMessage = message.toCharArray();

        for (int i = 0; i < charOfMessage.length; i++) {
            char symbol = charOfMessage[i];
            if (symbol == '#' && (i+6) < charOfMessage.length) {
                StringBuilder hex = new StringBuilder();
                for (int j = 0; j < 7; j++) {
                    hex.append(charOfMessage[i + j]);
                }
                replaced.append(ChatColor.of(hex.toString()));
                i = i + 6;
                continue;
            }
            replaced.append(symbol);
        }

        return replaced.toString();
    }
}
