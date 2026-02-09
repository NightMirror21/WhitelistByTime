package ru.nightmirror.wlbytime.identity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ResolvedPlayer(@NotNull PlayerKey key, @NotNull String nickname, @Nullable UUID uuid) {
    public boolean isUuidKey() {
        return key.uuid();
    }
}
