package ru.nightmirror.wlbytime.time;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class TimeRandom {

    TimeConvertor convertor;
    Random random = new Random();

    public String getRandomOneTime() {
        long ms = random.nextLong() + 1000L;
        return convertor.getTimeLine(ms).split(" ")[0];
    }
}
