package ru.nightmirror.wlbytime.interfaces.services;

import ru.nightmirror.wlbytime.entry.EntryImpl;

import java.time.Duration;
import java.time.Instant;

public interface EntryTimeService {
    void add(EntryImpl entry, Duration duration);

    boolean canAdd(EntryImpl entry, Duration duration);

    void remove(EntryImpl entry, Duration duration);

    boolean canRemove(EntryImpl entry, Duration duration);

    void set(EntryImpl entry, Instant instant);
}
