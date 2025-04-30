package ru.nightmirror.wlbytime.time;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeRandomTest {

    private TimeConvertor timeConvertor;
    private TimeRandom timeRandom;

    @BeforeEach
    public void setUp() {
        timeConvertor = mock(TimeConvertor.class);
        timeRandom = new TimeRandom(timeConvertor);

        when(timeConvertor.getTimeLine(any(Duration.class))).thenAnswer(invocation -> {
            Duration duration = invocation.getArgument(0);
            return duration.toString();
        });
    }

    @Test
    public void shouldGenerateRandomTime() {
        String randomTime = timeRandom.getRandomOneTime();

        assertThat(randomTime).isNotNull();
        assertThat(randomTime).isNotEmpty();
    }

    @Test
    public void shouldReturnSetOfTimes() {
        Set<String> times = timeRandom.getTimes();

        assertThat(times).isNotNull();
        assertThat(times).isNotEmpty();
        assertThat(times).hasSize(21); // 20 predefined + 1 random
    }

    @Test
    public void shouldIncludeRandomTimeInSetOfTimes() {
        when(timeConvertor.getTimeLine(any(Duration.class))).thenReturn("RANDOM_TIME");

        Set<String> times = timeRandom.getTimes();

        assertThat(times).contains("RANDOM_TIME");
    }
}