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

    @Getter
    long id;

    @Getter
    String nickname;
    long until;

    @Nullable
    @Builder.Default
    Long frozenAt = null;

    private State getState() {
        return State.get(until);
    }

    public boolean isFrozen() {
        return frozenAt != null;
    }

    public boolean isNotInWhitelist() {
        return getState().equals(State.NOT_IN_WHITELIST);
    }

    public boolean isForever() {
        return getState().equals(State.FOREVER);
    }

    public Timestamp getFrozenAt() {
        if (frozenAt == null) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        return new Timestamp(frozenAt);
    }

    public void freeze() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Entry is already frozen");
        }
        if (isForever()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        frozenAt = System.currentTimeMillis();
    }

    public void unfreeze() {
        if (!isFrozen()) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        long elapsedTime = System.currentTimeMillis() - frozenAt;
        until += elapsedTime;
        frozenAt = null;
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

    public boolean isExpired() {
        if (isForever()) {
            return false;
        }
        return until < System.currentTimeMillis();
    }

    public boolean isActive() {
        return !isExpired();
    }

    public Timestamp getUntil() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Entry is frozen");
        }
        if (isForever()) {
            throw new UnsupportedOperationException("Entry is forever");
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
        if (!isFrozen()) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        return frozenAt - System.currentTimeMillis();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    @RequiredArgsConstructor
    private enum State {
        FOREVER(-1L),
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