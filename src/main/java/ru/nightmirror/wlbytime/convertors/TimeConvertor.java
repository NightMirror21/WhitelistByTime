package ru.nightmirror.wlbytime.convertors;

import org.bukkit.plugin.Plugin;

public class TimeConvertor {

    public static String getTimeLine(Plugin plugin, long ms) {
        String line = "";

        if ((ms / 31536000000L) > 0L) {
            line = ms / 31536000000L + plugin.getConfig().getString("time-units.year") + " ";
            ms = ms - (ms / 31536000000L)*31536000000L;
        }

        if ((ms / 2592000000L) > 0L) {
            line = line + ms / 2592000000L + plugin.getConfig().getString("time-units.month") + " ";
            ms = ms - (ms / 2592000000L)*2592000000L;
        }

        if ((ms / 604800000L) > 0L) {
            line = line + ms / 604800000L + plugin.getConfig().getString("time-units.week") + " ";
            ms = ms - (ms / 604800000L)*604800000L;
        }

        if ((ms / 86400000L) > 0L) {
            line = line + ms / 86400000L + plugin.getConfig().getString("time-units.day") + " ";
            ms = ms - (ms / 86400000L)*86400000L;
        }

        if ((ms / 3600000L) > 0L) {
            line = line + ms / 3600000L + plugin.getConfig().getString("time-units.hour") + " ";
            ms = ms - (ms / 3600000L)*3600000L;
        }

        if ((ms / 60L) > 0L) {
            line = line + ms / 60000L + plugin.getConfig().getString("time-units.minute") + " ";
            ms = ms - (ms / 60000L)*60000L;
        }

        if (ms != 0L) line = line + (ms/1000L) + plugin.getConfig().getString("time-units.second") + " ";

        return line;
    }

    public static long getTimeMs(Plugin plugin, String line) {
        long time = 0;

        for (String timeStr : line.split(" ")) {

            // Year
            if (timeStr.endsWith(plugin.getConfig().getString("time-units.year", "y"))) {
                timeStr = timeStr.replaceAll(plugin.getConfig().getString("time-units.year", "y"), "");

                if (checkLong(timeStr)) {
                    time+=31536000000L*Long.parseLong(timeStr);
                }
            }

            // Month
            if (timeStr.endsWith(plugin.getConfig().getString("time-units.month", "mo"))) {
                timeStr = timeStr.replaceAll(plugin.getConfig().getString("time-units.month", "mo"), "");

                if (checkLong(timeStr)) {
                    time+=2592000000L*Long.parseLong(timeStr);
                }
            }

            // Week
            if (timeStr.endsWith(plugin.getConfig().getString("time-units.week", "w"))) {
                timeStr = timeStr.replaceAll(plugin.getConfig().getString("time-units.week", "w"), "");

                if (checkLong(timeStr)) {
                    time+=604800000L*Long.parseLong(timeStr);
                }
            }

            // Day
            if (timeStr.endsWith(plugin.getConfig().getString("time-units.day", "d"))) {
                timeStr = timeStr.replaceAll(plugin.getConfig().getString("time-units.day", "d"), "");

                if (checkLong(timeStr)) {
                    time+=86400000L*Long.parseLong(timeStr);
                }
            }

            // Hour
            if (timeStr.endsWith(plugin.getConfig().getString("time-units.hour", "h"))) {
                timeStr = timeStr.replaceAll(plugin.getConfig().getString("time-units.hour", "h"), "");

                if (checkLong(timeStr)) {
                    time+=3600000L*Long.parseLong(timeStr);
                }
            }

            // Minute
            if (timeStr.endsWith(plugin.getConfig().getString("time-units.minute", "m"))) {
                timeStr = timeStr.replaceAll(plugin.getConfig().getString("time-units.minute", "m"), "");

                if (checkLong(timeStr)) {
                    time+=60000L*Long.parseLong(timeStr);
                }
            }

            // Seconds
            if (timeStr.endsWith(plugin.getConfig().getString("time-units.second", "s"))) {
                timeStr = timeStr.replaceAll(plugin.getConfig().getString("time-units.second", "s"), "");

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
}
