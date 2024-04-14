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

    private static final Long UNTIL_FOREVER = -1L;
    private static final Long UNTIL_NOT_IN_WHITELIST = 0L;
    private static final Long NOT_FROZEN = -1L;

    @Nullable Long id;
    @NotNull String nickname;
    @NotNull Long until;
    @NotNull Long frozenAt;

    public PlayerData(@NotNull String nickname, @NotNull Long until) {
        this.nickname = nickname;
        this.until = until;
        frozenAt = NOT_FROZEN;
    }

    public void setUntil(Long until) {
        this.until = until;

        if (isFrozen()) {
            frozenAt = System.currentTimeMillis();
        }
    }

    public Long calculateUntil() {
        return frozenAt.equals(NOT_FROZEN) ? until : (until - frozenAt + System.currentTimeMillis());
    }

    public boolean isFrozen() {
        return !frozenAt.equals(NOT_FROZEN);
    }

    public boolean isForever() {
        return until.equals(UNTIL_FOREVER);
    }

    public void setNotInWhitelist() {
        if (isFrozen()) {
            frozenAt = NOT_FROZEN;
        }
        until = UNTIL_NOT_IN_WHITELIST;
    }

    public void setForever() {
        until = UNTIL_FOREVER;
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
            frozenAt = NOT_FROZEN;
        } else {
            frozenAt = System.currentTimeMillis();
        }
    }
}
