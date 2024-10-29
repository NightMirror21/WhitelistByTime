package ru.nightmirror.wlbytime.interfaces.services;

import ru.nightmirror.wlbytime.entry.Entry;

import java.util.Set;

public interface EntryService {
    void remove(Entry entry);

    Entry create(String nickname);

    Entry create(String nickname, long untilMs);

    void freeze(Entry entry, long durationMs);

    void unfreeze(Entry entry);

    Set<Entry> getEntries();
}
