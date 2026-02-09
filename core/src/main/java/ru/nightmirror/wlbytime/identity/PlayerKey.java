package ru.nightmirror.wlbytime.identity;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

public record PlayerKey(String value, boolean uuid) {

    public static @NotNull PlayerKey nickname(@NotNull String nickname) {
        return new PlayerKey(nickname, false);
    }

    public static @NotNull PlayerKey uuid(@NotNull UUID uuid) {
        return new PlayerKey(uuid.toString().toLowerCase(Locale.ROOT), true);
    }

    public static @NotNull PlayerKey uuid(@NotNull String uuid) {
        return new PlayerKey(uuid.toLowerCase(Locale.ROOT), true);
    }
}
