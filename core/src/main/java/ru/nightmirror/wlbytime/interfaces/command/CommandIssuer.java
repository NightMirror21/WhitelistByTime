package ru.nightmirror.wlbytime.interfaces.command;

import java.util.Optional;
import java.util.UUID;

public interface CommandIssuer {
    boolean isConsole();

    boolean isPlayer();

    String getNickname();

    Optional<UUID> getUuid();

    void sendMessage(String message);

    boolean hasPermission(String permission);
}
