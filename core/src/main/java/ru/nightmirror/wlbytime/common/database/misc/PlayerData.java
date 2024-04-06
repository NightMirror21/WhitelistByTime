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
    @NotNull Long frozenAt;

    public PlayerData(@NotNull String nickname, @NotNull Long until) {
        this.nickname = nickname;
        this.until = until;
        frozenAt = -1L;
    }

    public Long calculateUntil() {
        return frozenAt == -1L ? until : (until - frozenAt + System.currentTimeMillis());
    }

    public boolean isFrozen() {
        return frozenAt == -1L;
    }

    public boolean isForever() {
        return until == -1L;
    }

    public void switchFreeze() {
        if (until != -1L) {
            return;
        }

        if (isFrozen()) {
            until = calculateUntil();
            frozenAt = -1L;
        } else {
            frozenAt = System.currentTimeMillis();
        }
    }
}
