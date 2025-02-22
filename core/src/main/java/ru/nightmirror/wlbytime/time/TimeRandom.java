package ru.nightmirror.wlbytime.time;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class TimeRandom {

    static long MINIMUM_TIME = Duration.ofSeconds(1).toMillis();
    static long MAXIMUM_TIME = Duration.ofDays(365).toMillis();
    static List<Duration> TIMES = List.of(
            Duration.ofDays(365 * 2),
            Duration.ofDays(365),
            Duration.ofDays(31),
            Duration.ofDays(7),
            Duration.ofDays(5),
            Duration.ofDays(3),
            Duration.ofDays(2),
            Duration.ofDays(1),
            Duration.ofHours(12),
            Duration.ofHours(8),
            Duration.ofHours(1),
            Duration.ofMinutes(30),
            Duration.ofMinutes(15),
            Duration.ofMinutes(5),
            Duration.ofMinutes(1),
            Duration.ofSeconds(45),
            Duration.ofSeconds(30),
            Duration.ofSeconds(15),
            Duration.ofSeconds(5),
            Duration.ofSeconds(1)
    );

    TimeConvertor convertor;
    Random random = new Random();

    public String getRandomOneTime() {
        long ms = random.nextLong(MAXIMUM_TIME) + MINIMUM_TIME;
        return convertor.getTimeLine(Duration.ofMillis(ms));
    }

    public Set<String> getTimes() {
        List<String> formattedTimes = new ArrayList<>(TIMES.stream().map(convertor::getTimeLine).toList());
        formattedTimes.add(getRandomOneTime());
        return Set.copyOf(formattedTimes);
    }
}
