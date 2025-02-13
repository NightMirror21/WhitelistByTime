package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Freezing {
    long entryId;
    Instant startTime;
    Instant endTime;

    public Freezing(long entryId, Duration duration) {
        this.entryId = entryId;
        this.startTime = Instant.now();
        this.endTime = this.startTime.plus(duration);
    }

    public boolean isFrozen() {
        return endTime.isAfter(Instant.now());
    }

    public Duration getLeftTime() {
        return Duration.between(Instant.now(), endTime);
    }

    public Duration getDurationOfFreeze() {
        return Duration.between(startTime, endTime);
    }
}
