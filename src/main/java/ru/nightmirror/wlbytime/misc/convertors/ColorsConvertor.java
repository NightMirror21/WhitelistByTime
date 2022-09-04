package ru.nightmirror.wlbytime.misc.convertors;

import java.util.List;

public class ColorsConvertor {

    public static String convert(String text) {
        return text.replaceAll("&", "§");
    }

    public static List<String> convert(List<String> list) {
        list.replaceAll(s -> s.replaceAll("&", "§"));
        return list;
    }
}
