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

    public Freezing(long entryId, long durationMillis) {
        this.entryId = entryId;
        this.startTime = Instant.now();
        this.endTime = this.startTime.plusMillis(durationMillis);
    }

    public boolean isFrozen() {
        return endTime.isAfter(Instant.now());
    }

    public long getLeftTime() {
        return Duration.between(Instant.now(), endTime).toMillis();
    }

    public long getDurationOfFreeze() {
        return Duration.between(startTime, endTime).toMillis();
    }
}
