package ru.nightmirror.wlbytime.time;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class TimeConvertor {
    static long YEAR_IN_MS = 31536000000L;
    static long MONTH_IN_MS = 2592000000L;
    static long WEEK_IN_MS = 604800000L;
    static long DAY_IN_MS = 86400000L;
    static long HOUR_IN_MS = 3600000L;
    static long MINUTE_IN_MS = 60000L;
    static long SECOND_IN_MS = 1000L;

    TimeUnitsConvertorSettings settings;

    private static boolean endsWith(String string, Set<String> patterns) {
        return patterns.stream().anyMatch(string::endsWith);
    }

    private static String clear(String string, Set<String> patterns) {
        for (String pattern : patterns) {
            string = string.replaceAll(pattern, "");
        }
        return string;
    }

    private static boolean checkLong(String number) {
        try {
            Long.parseLong(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getTimeLine(long ms) {
        String line = "";

        line = appendTimeUnit(line, ms, YEAR_IN_MS, settings.getFirstYearOrDefault());
        ms %= YEAR_IN_MS;

        line = appendTimeUnit(line, ms, MONTH_IN_MS, settings.getFirstMonthOrDefault());
        ms %= MONTH_IN_MS;

        line = appendTimeUnit(line, ms, WEEK_IN_MS, settings.getFirstWeekOrDefault());
        ms %= WEEK_IN_MS;

        line = appendTimeUnit(line, ms, DAY_IN_MS, settings.getFirstDayOrDefault());
        ms %= DAY_IN_MS;

        line = appendTimeUnit(line, ms, HOUR_IN_MS, settings.getFirstHourOrDefault());
        ms %= HOUR_IN_MS;

        line = appendTimeUnit(line, ms, MINUTE_IN_MS, settings.getFirstMinuteOrDefault());
        ms %= MINUTE_IN_MS;

        if (ms != 0L) {
            line += (ms / 1000L) + settings.getFirstSecondOrDefault();
        }

        return ms < 0L ? settings.getForever() : line.trim();
    }

    private String appendTimeUnit(String line, long ms, long unitMs, String unitLabel) {
        if (ms / unitMs > 0L) {
            line += ms / unitMs + unitLabel + " ";
        }
        return line;
    }

    public long getTimeMs(String line) {
        long time = 0;
        for (String timeStr : line.split(" ")) {
            time += getTimeForUnit(timeStr, settings.getYear(), YEAR_IN_MS);
            time += getTimeForUnit(timeStr, settings.getMonth(), MONTH_IN_MS);
            time += getTimeForUnit(timeStr, settings.getWeek(), WEEK_IN_MS);
            time += getTimeForUnit(timeStr, settings.getDay(), DAY_IN_MS);
            time += getTimeForUnit(timeStr, settings.getHour(), HOUR_IN_MS);
            time += getTimeForUnit(timeStr, settings.getMinute(), MINUTE_IN_MS);
            time += getTimeForUnit(timeStr, settings.getSecond(), SECOND_IN_MS);
        }
        return time;
    }

    private long getTimeForUnit(String timeStr, Set<String> patterns, long unitMs) {
        if (endsWith(timeStr, patterns)) {
            timeStr = clear(timeStr, patterns);
            return checkLong(timeStr) ? unitMs * Long.parseLong(timeStr) : 0;
        }
        return 0;
    }
}