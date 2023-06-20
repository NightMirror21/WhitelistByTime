package ru.nightmirror.wlbytime.common.convertor;

import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class ColorsConvertor {

    public static String convert(String text) {
        return convertHex(text.replaceAll("&", "§"));
    }

    public static List<String> convert(List<String> list) {
        list.replaceAll(ColorsConvertor::convert);
        return list;
    }

    public static String convertHex(String message) {
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
