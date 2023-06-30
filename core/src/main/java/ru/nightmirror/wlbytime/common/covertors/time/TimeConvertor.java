package ru.nightmirror.wlbytime.common.covertors.time;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TimeConvertor {
    TimeUnitsConvertorSettings settings;

    public String getTimeLine(long ms) {
        String line = "";
        long fullMs = ms;

        if ((ms / 31536000000L) > 0L) {
            line = ms / 31536000000L + settings.getYear().get(0) + " ";
            ms = ms - (ms / 31536000000L)*31536000000L;
        }

        if ((ms / 2592000000L) > 0L) {
            line = line + ms / 2592000000L + settings.getMonth().get(0) + " ";
            ms = ms - (ms / 2592000000L)*2592000000L;
        }

        if ((ms / 604800000L) > 0L) {
            line = line + ms / 604800000L + settings.getWeek().get(0) + " ";
            ms = ms - (ms / 604800000L)*604800000L;
        }

        if ((ms / 86400000L) > 0L) {
            line = line + ms / 86400000L + settings.getDay().get(0) + " ";
            ms = ms - (ms / 86400000L)*86400000L;
        }

        if ((ms / 3600000L) > 0L) {
            line = line + ms / 3600000L + settings.getHour().get(0) + " ";
            ms = ms - (ms / 3600000L)*3600000L;
        }

        if ((ms / 60L) > 0L) {
            line = line + ms / 60000L + settings.getMinute().get(0) + " ";
            ms = ms - (ms / 60000L)*60000L;
        }

        if (ms != 0L) line = line + (ms/1000L) + settings.getSecond().get(0) + " ";

        if (ms < 0L) line = settings.getForever();

        return line.trim();
    }

    public long getTimeMs(String line) {
        long time = 0;

        for (String timeStr : line.split(" ")) {

            // Year
            if (endsWith(timeStr, settings.getYear())) {
                timeStr = clear(timeStr, settings.getYear());

                if (checkLong(timeStr)) {
                    time+=31536000000L*Long.parseLong(timeStr);
                }
            }

            // Month
            if (endsWith(timeStr, settings.getMonth())) {
                timeStr = clear(timeStr, settings.getMonth());

                if (checkLong(timeStr)) {
                    time+=2592000000L*Long.parseLong(timeStr);
                }
            }

            // Week
            if (endsWith(timeStr, settings.getWeek())) {
                timeStr = clear(timeStr, settings.getWeek());

                if (checkLong(timeStr)) {
                    time+=604800000L*Long.parseLong(timeStr);
                }
            }

            // Day
            if (endsWith(timeStr, settings.getDay())) {
                timeStr = clear(timeStr, settings.getDay());

                if (checkLong(timeStr)) {
                    time+=86400000L*Long.parseLong(timeStr);
                }
            }

            // Hour
            if (endsWith(timeStr, settings.getHour())) {
                timeStr = clear(timeStr, settings.getHour());

                if (checkLong(timeStr)) {
                    time+=3600000L*Long.parseLong(timeStr);
                }
            }

            // Minute
            if (endsWith(timeStr, settings.getMinute())) {
                timeStr = clear(timeStr, settings.getMinute());

                if (checkLong(timeStr)) {
                    time+=60000L*Long.parseLong(timeStr);
                }
            }

            // Second
            if (endsWith(timeStr, settings.getSecond())) {
                timeStr = clear(timeStr, settings.getSecond());

                if (checkLong(timeStr)) {
                    time+=1000L*Long.parseLong(timeStr);
                }
            }
        }

        return time;
    }

    public static boolean checkLong(String number) {
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
