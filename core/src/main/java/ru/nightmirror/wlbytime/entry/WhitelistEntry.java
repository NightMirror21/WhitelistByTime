package ru.nightmirror.wlbytime.entry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequiredArgsConstructor
public class WhitelistEntry {
    @Getter
    long id;
    @Getter
    String nickname;
    long until;
    Optional<Long> frozenAt;

    private State getState() {
        return State.get(until);
    }

    public boolean isFrozen() {
        return frozenAt.isPresent();
    }

    public boolean isNotInWhitelist() {
        return getState().equals(State.NOT_IN_WHITELIST);
    }

    public boolean isForever() {
        return getState().equals(State.FOREVER);
    }

    public Timestamp getFrozenAt() {
        return frozenAt.map(Timestamp::new)
                .orElseThrow(() -> new UnsupportedOperationException("Entry is not frozen"));
    }

    public void freeze() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Entry is already frozen");
        }
        if (isForever()) {
            throw new UnsupportedOperationException("Entry is forever");
        }
        frozenAt = Optional.of(System.currentTimeMillis());
    }

    public void unfreeze() {
        if (!isFrozen()) {
            throw new UnsupportedOperationException("Entry is not frozen");
        }
        long elapsedTime = System.currentTimeMillis() - frozenAt.orElseThrow();
        until += elapsedTime;
        frozenAt = Optional.empty();
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
