package ru.nightmirror.wlbytime.time;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeConvertorTest {

    private TimeConvertor timeConvertor;

    @BeforeEach
    public void setUp() {
        TimeUnitsConvertorSettings settings = mock(TimeUnitsConvertorSettings.class);
        when(settings.getFirstYearOrDefault()).thenReturn("y");
        when(settings.getFirstMonthOrDefault()).thenReturn("mo");
        when(settings.getFirstWeekOrDefault()).thenReturn("w");
        when(settings.getFirstDayOrDefault()).thenReturn("d");
        when(settings.getFirstHourOrDefault()).thenReturn("h");
        when(settings.getFirstMinuteOrDefault()).thenReturn("m");
        when(settings.getFirstSecondOrDefault()).thenReturn("s");
        when(settings.getForever()).thenReturn("forever");
        when(settings.getYear()).thenReturn(Set.of("y"));
        when(settings.getMonth()).thenReturn(Set.of("mo"));
        when(settings.getWeek()).thenReturn(Set.of("w"));
        when(settings.getDay()).thenReturn(Set.of("d"));
        when(settings.getHour()).thenReturn(Set.of("h"));
        when(settings.getMinute()).thenReturn(Set.of("m"));
        when(settings.getSecond()).thenReturn(Set.of("s"));

        timeConvertor = new TimeConvertor(settings);
    }

    @Test
    public void getTimeLineZeroMillisecondsReturnsEmptyString() {
        String result = timeConvertor.getTimeLine(Duration.ofMillis(0));
        assertEquals("", result);
    }

    @Test
    public void getTimeLineOneYearReturnsCorrectString() {
        String result = timeConvertor.getTimeLine(Duration.ofMillis(TimeConvertor.YEAR_IN_MS));
        assertEquals("1y", result.trim());
    }

    @Test
    public void getTimeLineComplexTimeReturnsCorrectString() {
        long ms = TimeConvertor.YEAR_IN_MS + 2 * TimeConvertor.MONTH_IN_MS + 3 * TimeConvertor.DAY_IN_MS +
                4 * TimeConvertor.HOUR_IN_MS + 5 * TimeConvertor.MINUTE_IN_MS + 6 * TimeConvertor.SECOND_IN_MS;
        String result = timeConvertor.getTimeLine(Duration.ofMillis(ms));
        assertEquals("1y 2mo 3d 4h 5m 6s", result.trim());
    }

    @Test
    public void getTimeLineNegativeTimeReturnsForever() {
        String result = timeConvertor.getTimeLine(Duration.ofMillis(-1L));
        assertEquals("forever", result);
    }

    @Test
    public void getTimeSimpleStringReturnsCorrectDuration() {
        String timeLine = "1y";
        Duration result = timeConvertor.getTime(timeLine);
        assertEquals(Duration.ofMillis(TimeConvertor.YEAR_IN_MS), result);
    }

    @Test
    public void getTimeComplexStringReturnsCorrectDuration() {
        String timeLine = "1y 2mo 3d 4h 5m 6s";
        long expectedMs = TimeConvertor.YEAR_IN_MS + 2 * TimeConvertor.MONTH_IN_MS + 3 * TimeConvertor.DAY_IN_MS +
                4 * TimeConvertor.HOUR_IN_MS + 5 * TimeConvertor.MINUTE_IN_MS + 6 * TimeConvertor.SECOND_IN_MS;
        Duration result = timeConvertor.getTime(timeLine);
        assertEquals(Duration.ofMillis(expectedMs), result);
    }

    @Test
    public void getTimeEmptyStringReturnsZeroDuration() {
        Duration result = timeConvertor.getTime("");
        assertEquals(Duration.ZERO, result);
    }

    @Test
    public void getTimeInvalidStringReturnsZeroDuration() {
        Duration result = timeConvertor.getTime("abc");
        assertEquals(Duration.ZERO, result);
    }
}
