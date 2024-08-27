package ru.nightmirror.wlbytime.common.database.misc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
@ToString
public class PlayerData {

    @Nullable Long id;
    @NotNull String nickname;
    @NotNull Long until;
    @Nullable Long frozenAt;

    public PlayerData(@NotNull String nickname, @NotNull Long until) {
        this.nickname = nickname;
        this.until = until;
        frozenAt = null;
    }

    public State getState() {
        return State.get(until);
    }

    public void setUntil(Long until) {
        this.until = until;

        if (isFrozen()) {
            frozenAt = System.currentTimeMillis();
        }
    }

    public Long calculateUntil() {
        return frozenAt == null ? until : (until - frozenAt + System.currentTimeMillis());
    }

    public boolean isFrozen() {
        return getState().equals(State.FROZEN);
    }

    public boolean isNotInWhitelist() {
        return getState().equals(State.NOT_IN_WHITELIST);
    }

    public boolean isForever() {
        return getState().equals(State.FOREVER);
    }

    public void setNotInWhitelist() {
        if (isFrozen()) {
            frozenAt = null;
        }
        until = State.NOT_IN_WHITELIST.getUntil();
    }

    public void setForever() {
        until = State.FOREVER.getUntil();
    }

    public boolean canPlay() {
        return isForever() || calculateUntil() > System.currentTimeMillis();
    }

    public void switchFreeze() {
        if (isForever()) {
            return;
        }

        if (isFrozen()) {
            until = calculateUntil();
            frozenAt = null;
        } else {
            frozenAt = System.currentTimeMillis();
        }
    }
}
