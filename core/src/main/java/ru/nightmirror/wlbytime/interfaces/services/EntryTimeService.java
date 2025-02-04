package ru.nightmirror.wlbytime.interfaces.services;

import ru.nightmirror.wlbytime.entry.EntryImpl;

public interface EntryTimeService {
    void add(EntryImpl entry, long milliseconds);

    boolean canAdd(EntryImpl entry, long milliseconds);

    void remove(EntryImpl entry, long milliseconds);

    boolean canRemove(EntryImpl entry, long milliseconds);

    void set(EntryImpl entry, long milliseconds);
}
