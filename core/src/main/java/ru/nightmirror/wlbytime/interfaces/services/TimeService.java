package ru.nightmirror.wlbytime.interfaces.services;

import ru.nightmirror.wlbytime.entry.Entry;

public interface TimeService {
    void add(Entry entry, long milliseconds);

    boolean canAdd(Entry entry, long milliseconds);

    void remove(Entry entry, long milliseconds);

    boolean canRemove(Entry entry, long milliseconds);

    void set(Entry entry, long milliseconds);
}
