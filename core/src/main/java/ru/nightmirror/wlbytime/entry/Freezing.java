package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

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
    @Nullable Instant pausedAt;

    public Freezing(long entryId, Duration duration) {
        this.entryId = entryId;
        this.startTime = Instant.now();
        this.endTime = this.startTime.plus(duration);
        this.pausedAt = null;
    }

    public boolean isPaused() {
        return pausedAt != null;
    }

    public boolean isFrozen() {
        if (isPaused()) {
            return endTime.isAfter(pausedAt);
        }
        return endTime.isAfter(Instant.now());
    }

    public Duration getLeftTime() {
        if (isPaused()) {
            return Duration.between(pausedAt, endTime);
        }
        return Duration.between(Instant.now(), endTime);
    }

    public Duration getDurationOfFreeze() {
        return Duration.between(startTime, endTime);
    }

    public void pause() {
        if (isPaused()) return;
        this.pausedAt = Instant.now();
    }

    public void resume() {
        if (!isPaused()) return;
        Duration offlineDuration = Duration.between(pausedAt, Instant.now());
        this.endTime = endTime.plus(offlineDuration);
        this.pausedAt = null;
    }
}
