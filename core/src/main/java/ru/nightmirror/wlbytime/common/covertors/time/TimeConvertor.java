package ru.nightmirror.wlbytime.common.covertors.time;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TimeConvertor {
    private static final long YEAR_IN_MS = 31536000000L;
    private static final long MONTH_IN_MS = 2592000000L;
    private static final long WEEK_IN_MS = 604800000L;
    private static final long DAY_IN_MS = 86400000L;
    private static final long HOUR_IN_MS = 3600000L;
    private static final long MINUTE_IN_MS = 60000L;

    TimeUnitsConvertorSettings settings;

    public static boolean checkLong(String number) {
        try {
            Long.parseLong(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

    public String getTimeLine(long ms) {
        String line = "";

        line = appendTimeUnit(line, ms, YEAR_IN_MS, settings.getYear().get(0));
        ms %= YEAR_IN_MS;

        line = appendTimeUnit(line, ms, MONTH_IN_MS, settings.getMonth().get(0));
        ms %= MONTH_IN_MS;

        line = appendTimeUnit(line, ms, WEEK_IN_MS, settings.getWeek().get(0));
        ms %= WEEK_IN_MS;

        line = appendTimeUnit(line, ms, DAY_IN_MS, settings.getDay().get(0));
        ms %= DAY_IN_MS;

        line = appendTimeUnit(line, ms, HOUR_IN_MS, settings.getHour().get(0));
        ms %= HOUR_IN_MS;

        line = appendTimeUnit(line, ms, MINUTE_IN_MS, settings.getMinute().get(0));
        ms %= MINUTE_IN_MS;

        if (ms != 0L) line += (ms / 1000L) + settings.getSecond().get(0) + " ";
        if (ms < 0L) line = settings.getForever();

        return line.trim();
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
            if (endsWith(timeStr, settings.getYear())) {
                time += parseTimeUnit(timeStr, settings.getYear(), YEAR_IN_MS);
            } else if (endsWith(timeStr, settings.getMonth())) {
                time += parseTimeUnit(timeStr, settings.getMonth(), MONTH_IN_MS);
            } else if (endsWith(timeStr, settings.getWeek())) {
                time += parseTimeUnit(timeStr, settings.getWeek(), WEEK_IN_MS);
            } else if (endsWith(timeStr, settings.getDay())) {
                time += parseTimeUnit(timeStr, settings.getDay(), DAY_IN_MS);
            } else if (endsWith(timeStr, settings.getHour())) {
                time += parseTimeUnit(timeStr, settings.getHour(), HOUR_IN_MS);
            } else if (endsWith(timeStr, settings.getMinute())) {
                time += parseTimeUnit(timeStr, settings.getMinute(), MINUTE_IN_MS);
            } else if (endsWith(timeStr, settings.getSecond())) {
                time += parseTimeUnit(timeStr, settings.getSecond(), 1000L);
            }
        }
        return time;
    }

    private long parseTimeUnit(String timeStr, List<String> unitPatterns, long unitMs) {
        timeStr = clear(timeStr, unitPatterns);
        if (checkLong(timeStr)) {
            return unitMs * Long.parseLong(timeStr);
        }
        return 0;
    }
}