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
    Timestamp frozenAt = null;

    @Nullable
    @Builder.Default
    Timestamp frozenUntil = null;

    @Nullable
    @Builder.Default
    @Getter
    Timestamp lastJoin = null;

    private State resolveCurrentState() {
        return State.get(until);
    }

    public boolean hasJoinedPreviously() {
        return lastJoin != null;
    }

    public void recordJoinTime() {
        lastJoin = new Timestamp(System.currentTimeMillis());
    }

    public boolean isCurrentlyFrozen() {
        return frozenAt != null && frozenUntil != null;
    }

    public boolean isExcludedFromWhitelist() {
        return resolveCurrentState().equals(State.NOT_IN_WHITELIST);
    }

    public boolean hasNoExpiration() {
        return resolveCurrentState().equals(State.FOREVER);
    }

    public Timestamp getFreezeStartTime() {
        if (frozenAt == null) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        return frozenAt;
    }

    public @Nullable Timestamp getFreezeStartTimeOrNull() {
        return frozenAt;
    }

    public Timestamp getFreezeEndTime() {
        if (frozenUntil == null) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        return frozenUntil;
    }

    public @Nullable Timestamp getFreezeEndTimeOrNull() {
        return frozenUntil;
    }

    public void applyFreeze(long howLongInMilliseconds) {
        if (isCurrentlyFrozen()) {
            throw new UnsupportedOperationException("Entry is already frozen");
        }
        if (hasNoExpiration()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        long currentTime = System.currentTimeMillis();
        frozenAt = new Timestamp(currentTime);
        frozenUntil = new Timestamp(currentTime + howLongInMilliseconds);
    }

    public void removeFreeze() {
        if (!isCurrentlyFrozen()) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        long elapsedTime = System.currentTimeMillis() - frozenUntil.getTime();
        until += elapsedTime;
        frozenAt = null;
        frozenUntil = null;
    }

    public void markAsNotInWhitelist() {
        if (isCurrentlyFrozen()) {
            throw new UnsupportedOperationException("Entry is frozen");
        }
        if (hasNoExpiration()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        until = State.NOT_IN_WHITELIST.getUntil();
    }

    public void setForever() {
        if (isCurrentlyFrozen()) {
            throw new UnsupportedOperationException("Entry is frozen");
        }
        if (isExcludedFromWhitelist()) {
            throw new UnsupportedOperationException("Entry is not in whitelist");
        }
        until = State.FOREVER.getUntil();
    }

    public boolean isExpiredConsideringFreeze() {
        if (hasNoExpiration()) {
            return false;
        }
        if (isCurrentlyFrozen() && frozenUntil != null && frozenAt != null) {
            long howLongFrozen = frozenUntil.getTime() - frozenAt.getTime();
            return howLongFrozen < 0;
        }
        return until < System.currentTimeMillis();
    }

    public boolean isCurrentlyActive() {
        return !isExpiredConsideringFreeze();
    }

    public Timestamp getEffectiveUntilTimestamp() {
        if (isCurrentlyFrozen()) {
            throw new UnsupportedOperationException("Entry is frozen");
        }
        if (hasNoExpiration()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        if (isCurrentlyFrozen() && frozenUntil != null && frozenAt != null) {
            long howLongFrozen = frozenUntil.getTime() - frozenAt.getTime();
            return new Timestamp(until + howLongFrozen);
        }
        return new Timestamp(until);
    }

    public long getRemainingActiveTime() {
        if (isCurrentlyFrozen()) {
            throw new UnsupportedOperationException("Entry is frozen");
        }
        if (hasNoExpiration()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        return until - System.currentTimeMillis();
    }

    public long getRemainingFreezeTime() {
        if (!isCurrentlyFrozen() || frozenUntil == null || frozenAt == null) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        return frozenUntil.getTime() - System.currentTimeMillis();
    }

    public long getUntilRaw() {
        return until;
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