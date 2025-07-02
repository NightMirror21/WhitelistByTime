package ru.nightmirror.wlbytime.time;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeUnitsConvertorSettingsTest {

    @Test
    public void shouldReturnFirstValueFromSet() {
        TimeUnitsConvertorSettings settings = TimeUnitsConvertorSettings.builder()
                .year(new LinkedHashSet<>() {{
                    add("year");
                    add("y");
                }})
                .month(new LinkedHashSet<>() {{
                    add("month");
                    add("mo");
                }})
                .week(new LinkedHashSet<>() {{
                    add("week");
                    add("w");
                }})
                .day(new LinkedHashSet<>() {{
                    add("day");
                    add("d");
                }})
                .hour(new LinkedHashSet<>() {{
                    add("hour");
                    add("h");
                }})
                .minute(new LinkedHashSet<>() {{
                    add("minute");
                    add("m");
                }})
                .second(new LinkedHashSet<>() {{
                    add("second");
                    add("s");
                }})
                .forever("forever")
                .build();

        assertThat(settings.getFirstYearOrDefault()).isEqualTo("year");
        assertThat(settings.getFirstMonthOrDefault()).isEqualTo("month");
        assertThat(settings.getFirstWeekOrDefault()).isEqualTo("week");
        assertThat(settings.getFirstDayOrDefault()).isEqualTo("day");
        assertThat(settings.getFirstHourOrDefault()).isEqualTo("hour");
        assertThat(settings.getFirstMinuteOrDefault()).isEqualTo("minute");
        assertThat(settings.getFirstSecondOrDefault()).isEqualTo("second");
        assertThat(settings.getForever()).isEqualTo("forever");
    }

    @Test
    public void shouldReturnDefaultValueWhenSetIsEmpty() {
        TimeUnitsConvertorSettings settings = TimeUnitsConvertorSettings.builder()
                .year(Collections.emptySet())
                .month(Collections.emptySet())
                .week(Collections.emptySet())
                .day(Collections.emptySet())
                .hour(Collections.emptySet())
                .minute(Collections.emptySet())
                .second(Collections.emptySet())
                .forever("forever")
                .build();

        assertThat(settings.getFirstYearOrDefault()).isEqualTo("y");
        assertThat(settings.getFirstMonthOrDefault()).isEqualTo("mo");
        assertThat(settings.getFirstWeekOrDefault()).isEqualTo("w");
        assertThat(settings.getFirstDayOrDefault()).isEqualTo("d");
        assertThat(settings.getFirstHourOrDefault()).isEqualTo("h");
        assertThat(settings.getFirstMinuteOrDefault()).isEqualTo("m");
        assertThat(settings.getFirstSecondOrDefault()).isEqualTo("s");
        assertThat(settings.getForever()).isEqualTo("forever");
    }

    @Test
    public void shouldReturnCorrectValuesFromGetters() {
        Set<String> yearSet = new LinkedHashSet<>(Set.of("year", "y"));
        Set<String> monthSet = new LinkedHashSet<>(Set.of("month", "mo"));
        Set<String> weekSet = new LinkedHashSet<>(Set.of("week", "w"));
        Set<String> daySet = new LinkedHashSet<>(Set.of("day", "d"));
        Set<String> hourSet = new LinkedHashSet<>(Set.of("hour", "h"));
        Set<String> minuteSet = new LinkedHashSet<>(Set.of("minute", "m"));
        Set<String> secondSet = new LinkedHashSet<>(Set.of("second", "s"));
        String forever = "forever";

        TimeUnitsConvertorSettings settings = TimeUnitsConvertorSettings.builder()
                .year(yearSet)
                .month(monthSet)
                .week(weekSet)
                .day(daySet)
                .hour(hourSet)
                .minute(minuteSet)
                .second(secondSet)
                .forever(forever)
                .build();

        assertThat(settings.getYear()).isEqualTo(yearSet);
        assertThat(settings.getMonth()).isEqualTo(monthSet);
        assertThat(settings.getWeek()).isEqualTo(weekSet);
        assertThat(settings.getDay()).isEqualTo(daySet);
        assertThat(settings.getHour()).isEqualTo(hourSet);
        assertThat(settings.getMinute()).isEqualTo(minuteSet);
        assertThat(settings.getSecond()).isEqualTo(secondSet);
        assertThat(settings.getForever()).isEqualTo(forever);
    }
}
