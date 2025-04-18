package ru.nightmirror.wlbytime.time;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter
@Builder
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
        return year.stream().findFirst().orElse("y");
    }

    public String getFirstMonthOrDefault() {
        return month.stream().findFirst().orElse("mo");
    }

    public String getFirstWeekOrDefault() {
        return week.stream().findFirst().orElse("w");
    }

    public String getFirstDayOrDefault() {
        return day.stream().findFirst().orElse("d");
    }

    public String getFirstHourOrDefault() {
        return hour.stream().findFirst().orElse("h");
    }

    public String getFirstMinuteOrDefault() {
        return minute.stream().findFirst().orElse("m");
    }

    public String getFirstSecondOrDefault() {
        return second.stream().findFirst().orElse("s");
    }
}
