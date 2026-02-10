package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.interfaces.entry.Entry;

import java.time.Duration;
import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
@EqualsAndHashCode(of = {"id", "nickname"})
public class EntryImpl implements Entry {
    long id;
    String nickname;
    String uuid;
    @Builder.Default
    Expiration expiration = null;
    @Builder.Default
    Freezing freezing = null;
    @Builder.Default
    LastJoin lastJoin = null;

    @Override
    public boolean isForever() {
        return expiration == null;
    }

    @Override
    public boolean isActive() {
        if (isForever()) {
            return true;
        }

        if (expiration.isNotExpired()) {
            return true;
        }

        if (isFreezeActive()) {
            return expiration.isNotExpired(freezing.getLeftTime());
        } else {
            return false;
        }
    }

    @Override
    public void setForever() {
        expiration = null;
    }

    @Override
    public void setExpiration(@NotNull Instant instant) {
        expiration = new Expiration(id, instant);
    }

    @Override
    public boolean isFrozen() {
        return freezing != null;
    }

    @Override
    public boolean isFreezeActive() {
        return isFrozen() && freezing.isFrozen();
    }

    @Override
    public boolean isFreezeInactive() {
        return isFrozen() && !freezing.isFrozen();
    }

    @Override
    public void freeze(Duration duration) {
        if (isFreezeActive()) {
            throw new IllegalStateException("Entry is already frozen.");
        }

        if (isForever()) {
            throw new IllegalStateException("Can't freeze forever entry.");
        }

        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Duration must be positive.");
        }

        freezing = new Freezing(id, duration);
    }

    @Override
    public void unfreeze() {
        if (isNotFrozen()) {
            throw new IllegalStateException("Entry is not frozen.");
        }

        Instant previousExpirationTime = expiration.getExpirationTime();
        Duration durationOfFreeze = freezing.getDurationOfFreeze();
        expiration = new Expiration(id, previousExpirationTime.plus(durationOfFreeze));

        freezing = null;
    }

    @Override
    public void updateLastJoin() {
        lastJoin = new LastJoin(id);
    }

    @Override
    public boolean isJoined() {
        return lastJoin != null;
    }

    @Override
    public Duration getLeftActiveDuration() {
        Duration offset = Duration.ZERO;

        if (isFreezeActive()) {
            offset = freezing.getLeftTime();
        }

        if (isForever()) {
            throw new IllegalStateException("Can't get left expiration time cause entry is forever");
        }

        return Duration.between(Instant.now(), expiration.getExpirationTime()).plus(offset);
    }

    @Override
    public Duration getLeftFreezeDuration() {
        if (isFreezeInactive()) {
            throw new IllegalStateException("Can't get left freeze time cause entry is not frozen");
        }
        return freezing.getLeftTime();
    }
}
