package ru.nightmirror.wlbytime.main;

public class Util {

    public static String getTimeLine(long ms) {
        Config config = Config.getInstance();
        String line = "";

        if ((ms / 31536000000L) > 0L) {
            line = ms / 31536000000L + config.getLine("timeform.year") + " ";
            ms = ms - (ms / 31536000000L)*31536000000L;
        }

        if ((ms / 2592000000L) > 0L) {
            line = line + ms / 2592000000L + config.getLine("timeform.month") + " ";
            ms = ms - (ms / 2592000000L)*2592000000L;
        }

        if ((ms / 604800000L) > 0L) {
            line = line + ms / 604800000L + config.getLine("timeform.week") + " ";
            ms = ms - (ms / 604800000L)*604800000L;
        }

        if ((ms / 86400000L) > 0L) {
            line = line + ms / 86400000L + config.getLine("timeform.day") + " ";
            ms = ms - (ms / 86400000L)*86400000L;
        }

        if ((ms / 3600000L) > 0L) {
            line = line + ms / 3600000L + config.getLine("timeform.hour") + " ";
            ms = ms - (ms / 3600000L)*3600000L;
        }

        if ((ms / 60L) > 0L) {
            line = line + ms / 60000L + config.getLine("timeform.minute") + " ";
            ms = ms - (ms / 60000L)*60000L;
        }

        if (ms != 0L) line = line + (ms/1000L) + config.getLine("timeform.second") + " ";

        return line;
    }

    public static long getTimeMs(String line) {
        Config config = Config.getInstance();
        long time = 0;

        for (String timeStr : line.split(" ")) {

            // Year
            if (timeStr.endsWith(config.getLine("timeform.year"))) {
                timeStr = timeStr.replaceAll(config.getLine("timeform.year"), "");

                if (checkLong(timeStr)) {
                    time+=31536000000L*Long.parseLong(timeStr);
                }
            }

            // Month
            if (timeStr.endsWith(config.getLine("timeform.month"))) {
                timeStr = timeStr.replaceAll(config.getLine("timeform.month"), "");

                if (checkLong(timeStr)) {
                    time+=2592000000L*Long.parseLong(timeStr);
                }
            }

            // Week
            if (timeStr.endsWith(config.getLine("timeform.week"))) {
                timeStr = timeStr.replaceAll(config.getLine("timeform.week"), "");

                if (checkLong(timeStr)) {
                    time+=604800000L*Long.parseLong(timeStr);
                }
            }

            // Day
            if (timeStr.endsWith(config.getLine("timeform.day"))) {
                timeStr = timeStr.replaceAll(config.getLine("timeform.day"), "");

                if (checkLong(timeStr)) {
                    time+=86400000L*Long.parseLong(timeStr);
                }
            }

            // Hour
            if (timeStr.endsWith(config.getLine("timeform.hour"))) {
                timeStr = timeStr.replaceAll(config.getLine("timeform.hour"), "");

                if (checkLong(timeStr)) {
                    time+=3600000L*Long.parseLong(timeStr);
                }
            }

            // Minute
            if (timeStr.endsWith(config.getLine("timeform.minute"))) {
                timeStr = timeStr.replaceAll(config.getLine("timeform.minute"), "");

                if (checkLong(timeStr)) {
                    time+=60000L*Long.parseLong(timeStr);
                }
            }

            // Seconds
            if (timeStr.endsWith(config.getLine("timeform.second"))) {
                timeStr = timeStr.replaceAll(config.getLine("timeform.second"), "");

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
