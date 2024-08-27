package ru.nightmirror.wlbytime.common.database.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum State {
    FOREVER(-1L),
    FROZEN(-2L),
    NOT_IN_WHITELIST(-3L),
    TIME(null);

    Long until;

    public static State get(Long time) {
        return Arrays.stream(State.values())
                .filter(state -> state.getUntil() != null && state.getUntil().equals(time))
                .findFirst()
                .orElse(TIME);
    }
}
