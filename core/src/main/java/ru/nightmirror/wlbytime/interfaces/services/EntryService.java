package ru.nightmirror.wlbytime.interfaces.services;

import ru.nightmirror.wlbytime.entry.Entry;

public interface EntryService {
    void remove(Entry entry);

    Entry create(String nickname);

    Entry create(String nickname, long untilMs);

    void freeze(Entry entry, long durationMs);

    void unfreeze(Entry entry);
}
