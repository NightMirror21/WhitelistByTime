package ru.nightmirror.wlbytime.interfaces.entry;

import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

public interface Entry extends EntryView {
    void setForever();

    void setExpiration(@NotNull Timestamp timestamp);

    void freeze(long time);

    void unfreeze();

    void updateLastJoin();
}
