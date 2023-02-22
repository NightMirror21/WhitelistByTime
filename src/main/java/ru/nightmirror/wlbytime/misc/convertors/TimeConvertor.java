package ru.nightmirror.wlbytime.misc.convertors;

import org.bukkit.plugin.Plugin;

import java.util.List;

public class TimeConvertor {

    public static String getTimeLine(Plugin plugin, long ms) {
        String line = "";

        if ((ms / 31536000000L) > 0L) {
            line = ms / 31536000000L + plugin.getConfig().getStringList("time-units.year").get(0) + " ";
            ms = ms - (ms / 31536000000L)*31536000000L;
        }

        if ((ms / 2592000000L) > 0L) {
            line = line + ms / 2592000000L + plugin.getConfig().getStringList("time-units.month").get(0) + " ";
            ms = ms - (ms / 2592000000L)*2592000000L;
        }

        if ((ms / 604800000L) > 0L) {
            line = line + ms / 604800000L + plugin.getConfig().getStringList("time-units.week").get(0) + " ";
            ms = ms - (ms / 604800000L)*604800000L;
        }

        if ((ms / 86400000L) > 0L) {
            line = line + ms / 86400000L + plugin.getConfig().getStringList("time-units.day").get(0) + " ";
            ms = ms - (ms / 86400000L)*86400000L;
        }

        if ((ms / 3600000L) > 0L) {
            line = line + ms / 3600000L + plugin.getConfig().getStringList("time-units.hour").get(0) + " ";
            ms = ms - (ms / 3600000L)*3600000L;
        }

        if ((ms / 60L) > 0L) {
            line = line + ms / 60000L + plugin.getConfig().getStringList("time-units.minute").get(0) + " ";
            ms = ms - (ms / 60000L)*60000L;
        }

        if (ms != 0L) line = line + (ms/1000L) + plugin.getConfig().getStringList("time-units.second").get(0) + " ";

        return line.trim();
    }

    public static long getTimeMs(Plugin plugin, String line) {
        long time = 0;

        final List<String> yearUnits = plugin.getConfig().getStringList("time-units.year");
        final List<String> monthUnits = plugin.getConfig().getStringList("time-units.month");
        final List<String> weekUnits = plugin.getConfig().getStringList("time-units.week");
        final List<String> dayUnits = plugin.getConfig().getStringList("time-units.day");
        final List<String> hourUnits = plugin.getConfig().getStringList("time-units.hour");
        final List<String> minuteUnits = plugin.getConfig().getStringList("time-units.minute");
        final List<String> secondUnits = plugin.getConfig().getStringList("time-units.second");

        for (String timeStr : line.split(" ")) {

            // Year
            if (endsWith(timeStr, yearUnits)) {
                timeStr = clear(timeStr, yearUnits);

                if (checkLong(timeStr)) {
                    time+=31536000000L*Long.parseLong(timeStr);
                }
            }

            // Month
            if (endsWith(timeStr, monthUnits)) {
                timeStr = clear(timeStr, monthUnits);

                if (checkLong(timeStr)) {
                    time+=2592000000L*Long.parseLong(timeStr);
                }
            }

            // Week
            if (endsWith(timeStr, weekUnits)) {
                timeStr = clear(timeStr, weekUnits);

                if (checkLong(timeStr)) {
                    time+=604800000L*Long.parseLong(timeStr);
                }
            }

            // Day
            if (endsWith(timeStr, dayUnits)) {
                timeStr = clear(timeStr, dayUnits);

                if (checkLong(timeStr)) {
                    time+=86400000L*Long.parseLong(timeStr);
                }
            }

            // Hour
            if (endsWith(timeStr, hourUnits)) {
                timeStr = clear(timeStr, hourUnits);

                if (checkLong(timeStr)) {
                    time+=3600000L*Long.parseLong(timeStr);
                }
            }

            // Minute
            if (endsWith(timeStr, minuteUnits)) {
                timeStr = clear(timeStr, minuteUnits);

                if (checkLong(timeStr)) {
                    time+=60000L*Long.parseLong(timeStr);
                }
            }

            // Second
            if (endsWith(timeStr, secondUnits)) {
                timeStr = clear(timeStr, secondUnits);

                if (checkLong(timeStr)) {
                    time+=1000L*Long.parseLong(timeStr);
                }
            }
        }

        return time;
    }

    public static Boolean checkLong(String number) {
        try {
            Long.parseLong(number);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean endsWith(String string, List<String> patterns) {
        return patterns.stream().anyMatch(string::endsWith);
    }

    private static String clear(String string, List<String> patterns) {
        for (String pattern : patterns) {
            string = string.replaceAll(pattern, "");
        }
        return string;
    }
}
