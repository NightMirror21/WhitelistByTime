package ru.nightmirror.wlbytime.main;

public class Util {

    public static String getTimeLine(long ms) {
        Config config = Config.getInstance();
        String line = "";

        if ((ms / 31536000000L) > 0L) {
            line = ms / 31536000000L + config.getLine("time-units.year") + " ";
            ms = ms - (ms / 31536000000L)*31536000000L;
        }

        if ((ms / 2592000000L) > 0L) {
            line = line + ms / 2592000000L + config.getLine("time-units.month") + " ";
            ms = ms - (ms / 2592000000L)*2592000000L;
        }

        if ((ms / 604800000L) > 0L) {
            line = line + ms / 604800000L + config.getLine("time-units.week") + " ";
            ms = ms - (ms / 604800000L)*604800000L;
        }

        if ((ms / 86400000L) > 0L) {
            line = line + ms / 86400000L + config.getLine("time-units.day") + " ";
            ms = ms - (ms / 86400000L)*86400000L;
        }

        if ((ms / 3600000L) > 0L) {
            line = line + ms / 3600000L + config.getLine("time-units.hour") + " ";
            ms = ms - (ms / 3600000L)*3600000L;
        }

        if ((ms / 60L) > 0L) {
            line = line + ms / 60000L + config.getLine("time-units.minute") + " ";
            ms = ms - (ms / 60000L)*60000L;
        }

        if (ms != 0L) line = line + (ms/1000L) + config.getLine("time-units.second") + " ";

        return line;
    }

    public static long getTimeMs(String line) {
        Config config = Config.getInstance();
        long time = 0;

        for (String timeStr : line.split(" ")) {

            // Year
            if (timeStr.endsWith(config.getLine("time-units.year"))) {
                timeStr = timeStr.replaceAll(config.getLine("time-units.year"), "");

                if (checkLong(timeStr)) {
                    time+=31536000000L*Long.parseLong(timeStr);
                }
            }

            // Month
            if (timeStr.endsWith(config.getLine("time-units.month"))) {
                timeStr = timeStr.replaceAll(config.getLine("time-units.month"), "");

                if (checkLong(timeStr)) {
                    time+=2592000000L*Long.parseLong(timeStr);
                }
            }

            // Week
            if (timeStr.endsWith(config.getLine("time-units.week"))) {
                timeStr = timeStr.replaceAll(config.getLine("time-units.week"), "");

                if (checkLong(timeStr)) {
                    time+=604800000L*Long.parseLong(timeStr);
                }
            }

            // Day
            if (timeStr.endsWith(config.getLine("time-units.day"))) {
                timeStr = timeStr.replaceAll(config.getLine("time-units.day"), "");

                if (checkLong(timeStr)) {
                    time+=86400000L*Long.parseLong(timeStr);
                }
            }

            // Hour
            if (timeStr.endsWith(config.getLine("time-units.hour"))) {
                timeStr = timeStr.replaceAll(config.getLine("time-units.hour"), "");

                if (checkLong(timeStr)) {
                    time+=3600000L*Long.parseLong(timeStr);
                }
            }

            // Minute
            if (timeStr.endsWith(config.getLine("time-units.minute"))) {
                timeStr = timeStr.replaceAll(config.getLine("time-units.minute"), "");

                if (checkLong(timeStr)) {
                    time+=60000L*Long.parseLong(timeStr);
                }
            }

            // Seconds
            if (timeStr.endsWith(config.getLine("time-units.second"))) {
                timeStr = timeStr.replaceAll(config.getLine("time-units.second"), "");

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
