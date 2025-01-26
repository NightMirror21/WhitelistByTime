package ru.nightmirror.wlbytime.time;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class TimeRandom {

    static long MINIMUM_TIME = 1000L;

    TimeConvertor convertor;
    Random random = new Random();

    public String getRandomOneTime() {
        long ms = random.nextLong() + MINIMUM_TIME;
        return convertor.getTimeLine(ms).split(" ")[0];
    }
}
