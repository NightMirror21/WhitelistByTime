package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.Arrays;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id", "nickname"})
public class Entry {

    public static final long FOREVER = -1L;

    @Getter
    long id;

    @Getter
    String nickname;
    long until;

    @Nullable
    @Builder.Default
    Long frozenAt = null;

    @Nullable
    @Builder.Default
    Long frozenUntil = null;

    private State getState() {
        return State.get(until);
    }

    public boolean isFrozen() {
        return frozenAt != null && frozenUntil != null;
    }

    public boolean isNotInWhitelist() {
        return getState().equals(State.NOT_IN_WHITELIST);
    }

    public boolean isForever() {
        return getState().equals(State.FOREVER);
    }

    public Long getFrozenAtOrNull() {
        return frozenAt;
    }

    public Long getFrozenUntilOrNull() {
        return frozenUntil;
    }

    public Long getUntilOrNull() {
        return until;
    }

    public Timestamp getFrozenAt() {
        if (frozenAt == null) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        return new Timestamp(frozenAt);
    }

    public Timestamp getFrozenUntil() {
        if (frozenUntil == null || frozenAt == null) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        return new Timestamp(frozenUntil);
    }

    public void freeze(long howLongInMilliseconds) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Entry is already frozen");
        }
        if (isForever()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        frozenAt = System.currentTimeMillis();
        frozenUntil = System.currentTimeMillis() + howLongInMilliseconds;
    }

    public void unfreeze() {
        if (!isFrozen() || frozenUntil == null || frozenAt == null) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        long elapsedTime = System.currentTimeMillis() - frozenUntil;
        until += elapsedTime;
        frozenAt = null;
        frozenUntil = null;
    }

    public void setNotInWhitelist() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Entry is frozen");
        }
        if (isForever()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        until = State.NOT_IN_WHITELIST.getUntil();
    }

    public void setForever() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Entry is frozen");
        }
        if (isNotInWhitelist()) {
            throw new UnsupportedOperationException("Entry is not in whitelist");
        }
        until = State.FOREVER.getUntil();
    }

    public boolean isExpiredIncludeFreeze() {
        if (isForever()) {
            return false;
        }
        if (isFrozen() && frozenUntil != null && frozenAt != null) {
            long howLongFrozen = frozenUntil - frozenAt;
            return howLongFrozen < 0;
        }
        return until < System.currentTimeMillis();
    }

    public boolean isActive() {
        return !isExpiredIncludeFreeze();
    }

    public Timestamp getUntilIncludeFreeze() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Entry is frozen");
        }
        if (isForever()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        if (isFrozen() && frozenUntil != null && frozenAt != null) {
            long howLongFrozen = frozenUntil - frozenAt;
            return new Timestamp(until + howLongFrozen);
        }
        return new Timestamp(until);
    }

    public long getRemainingTime() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Entry is frozen");
        }
        if (isForever()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        return until - System.currentTimeMillis();
    }

    public long getRemainingTimeOfFreeze() {
        if (!isFrozen() || frozenUntil == null || frozenAt == null) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        return frozenUntil - System.currentTimeMillis();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    @RequiredArgsConstructor
    private enum State {
        FOREVER(Entry.FOREVER),
        NOT_IN_WHITELIST(-2L),
        TIME(null);

        Long until;

        private static State get(Long time) {
            return Arrays.stream(State.values())
                    .filter(state -> state.getUntil() != null && state.getUntil().equals(time))
                    .findFirst()
                    .orElse(TIME);
        }
    }
}