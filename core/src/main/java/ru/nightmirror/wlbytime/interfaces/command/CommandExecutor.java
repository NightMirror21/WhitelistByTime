package ru.nightmirror.wlbytime.interfaces.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface CommandExecutor {
    boolean isConsole();

    boolean isPlayer();

    @NotNull String getNickname();

    @Nullable UUID getUuid();

    void sendMessage(@NotNull String message);

    boolean hasPermission(@NotNull String permission);

    List<String> getAllPlayerNicknamesOnServer();
}
