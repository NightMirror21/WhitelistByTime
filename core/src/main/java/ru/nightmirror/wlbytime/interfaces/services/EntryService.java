package ru.nightmirror.wlbytime.interfaces.services;

import ru.nightmirror.wlbytime.entry.Entry;

public interface EntryService {
    void remove(Entry entry);

    Entry add(String nickname);

    Entry add(String nickname, long milliseconds);
}
