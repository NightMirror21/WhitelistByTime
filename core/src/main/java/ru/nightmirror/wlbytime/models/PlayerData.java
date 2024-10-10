package ru.nightmirror.wlbytime.models;

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
        this.frozenAt = null;
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

    public Long calculateElapsedTime() {
        long currentTime = System.currentTimeMillis();
        return frozenAt == null ? until : (until - frozenAt + currentTime);
    }

    public boolean isFrozen() {
        return isState(State.FROZEN);
    }

    private boolean isState(State state) {
        return getState().equals(state);
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
        long currentTime = System.currentTimeMillis();
        return isState(State.FOREVER) || calculateElapsedTime() > currentTime;
    }

    public void toggleFrozenState() {
        long currentTime = System.currentTimeMillis();
        if (isState(State.FOREVER)) {
            return;
        }
        if (isFrozen()) {
            until = calculateElapsedTime();
            frozenAt = null;
        } else {
            frozenAt = currentTime;
        }
    }
}