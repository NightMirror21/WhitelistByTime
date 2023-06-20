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
        StringBuilder lineBuilder = new StringBuilder();

        if (ms >= 31536000000L) {
            long years = ms / 31536000000L;
            lineBuilder.append(years).append(settings.getYear().get(0)).append(" ");
            ms -= years * 31536000000L;
        }

        if (ms >= 2592000000L) {
            long months = ms / 2592000000L;
            lineBuilder.append(months).append(settings.getMonth().get(0)).append(" ");
            ms -= months * 2592000000L;
        }

        if (ms >= 604800000L) {
            long weeks = ms / 604800000L;
            lineBuilder.append(weeks).append(settings.getWeek().get(0)).append(" ");
            ms -= weeks * 604800000L;
        }

        if (ms >= 86400000L) {
            long days = ms / 86400000L;
            lineBuilder.append(days).append(settings.getDay().get(0)).append(" ");
            ms -= days * 86400000L;
        }

        if (ms >= 3600000L) {
            long hours = ms / 3600000L;
            lineBuilder.append(hours).append(settings.getHour().get(0)).append(" ");
            ms -= hours * 3600000L;
        }

        if (ms >= 60000L) {
            long minutes = ms / 60000L;
            lineBuilder.append(minutes).append(settings.getMinute().get(0)).append(" ");
            ms -= minutes * 60000L;
        }

        if (ms >= 1000L) {
            long seconds = ms / 1000L;
            lineBuilder.append(seconds).append(settings.getSecond().get(0)).append(" ");
        }

        String line = lineBuilder.toString().trim();
        if (ms < 0L) {
            line = settings.getForever();
        }

        return line;
    }

    public long getTimeMs(String line) {
        long time = 0L;
        String[] parts = line.split(" ");

        for (String timeStr : parts) {
            timeStr = removeSuffixes(timeStr, settings.getYear());
            timeStr = removeSuffixes(timeStr, settings.getMonth());
            timeStr = removeSuffixes(timeStr, settings.getWeek());
            timeStr = removeSuffixes(timeStr, settings.getDay());
            timeStr = removeSuffixes(timeStr, settings.getHour());
            timeStr = removeSuffixes(timeStr, settings.getMinute());
            timeStr = removeSuffixes(timeStr, settings.getSecond());

            if (checkLong(timeStr)) {
                time += parseTime(timeStr);
            }
        }

        return time;
    }

    private boolean checkLong(String number) {
        try {
            Long.parseLong(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String removeSuffixes(String string, List<String> suffixes) {
        for (String suffix : suffixes) {
            string = string.replaceAll(suffix, "");
        }
        return string;
    }

    private long parseTime(String timeStr) {
        return Long.parseLong(timeStr) * 1000L;
    }
}
