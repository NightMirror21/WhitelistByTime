package ru.nightmirror.wlbytime.interfaces.dao;

import ru.nightmirror.wlbytime.entry.Entry;

import java.util.Optional;
import java.util.Set;

public interface EntryDao {
    void update(Entry entry);

    Optional<Entry> getLike(String nickname);

    Optional<Entry> get(String nickname);

    Entry create(String nickname, long milliseconds);

    Entry create(String nickname);

    void remove(Entry entry);

    default void create(Set<String> nicknames) {
        nicknames.forEach(this::create);
    }

    Set<Entry> getAll();
}
