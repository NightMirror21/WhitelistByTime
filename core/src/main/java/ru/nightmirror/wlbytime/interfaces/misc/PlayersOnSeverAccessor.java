package ru.nightmirror.wlbytime.interfaces.misc;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PlayersOnSeverAccessor {
    List<String> getPlayersOnServer();
    void kickPlayer(@NotNull String nickname);
    boolean isCaseSensitiveEnabled();
}
