package ru.nightmirror.wlbytime.interfaces.command.wrappers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface WrappedCommandSender {
    boolean isConsole();

    boolean isPlayer();

    @NotNull String getNickname();

    @Nullable UUID getUuid();

    void sendMessage(@NotNull String message);

    boolean hasPermission(@NotNull String permission);

    List<String> getAllPlayerNicknamesOnServer();
}
