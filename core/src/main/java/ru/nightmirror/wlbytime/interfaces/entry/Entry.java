package ru.nightmirror.wlbytime.interfaces.entry;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public interface Entry extends EntryView {
    void setForever();

    void setExpiration(@NotNull Instant instant);

    void freeze(Duration duration);

    void unfreeze();

    void updateLastJoin();
}
