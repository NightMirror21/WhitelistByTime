package ru.nightmirror.wlbytime.interfaces.database;

import ru.nightmirror.wlbytime.entry.Entry;

import java.util.Optional;

public interface EntryDao {
    void update(Entry entry);

    Optional<Entry> getLike(String nickname);

    Optional<Entry> get(String nickname);
}
