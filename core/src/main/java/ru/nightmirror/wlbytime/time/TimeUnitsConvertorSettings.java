package ru.nightmirror.wlbytime.time;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Builder
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class TimeUnitsConvertorSettings {

    Set<String> year;
    Set<String> month;
    Set<String> week;
    Set<String> day;
    Set<String> hour;
    Set<String> minute;
    Set<String> second;
    String forever;

    public String getFirstYearOrDefault() {
        return getFirstOrDefault(year, "y");
    }

    public String getFirstMonthOrDefault() {
        return getFirstOrDefault(month, "mo");
    }

    public String getFirstWeekOrDefault() {
        return getFirstOrDefault(week, "w");
    }

    public String getFirstDayOrDefault() {
        return getFirstOrDefault(day, "d");
    }

    public String getFirstHourOrDefault() {
        return getFirstOrDefault(hour, "h");
    }

    public String getFirstMinuteOrDefault() {
        return getFirstOrDefault(minute, "m");
    }

    public String getFirstSecondOrDefault() {
        return getFirstOrDefault(second, "s");
    }

    private String getFirstOrDefault(Set<String> set, String defaultValue) {
        return set.stream().findFirst().orElse(defaultValue);
    }
}

