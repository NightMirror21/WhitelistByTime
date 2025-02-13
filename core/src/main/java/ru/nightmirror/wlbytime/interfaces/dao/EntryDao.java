package ru.nightmirror.wlbytime.interfaces.dao;

import ru.nightmirror.wlbytime.entry.EntryImpl;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface EntryDao {
    void update(EntryImpl entry);

    Optional<EntryImpl> getLike(String nickname);

    Optional<EntryImpl> get(String nickname);

    EntryImpl create(String nickname, Instant until);

    EntryImpl create(String nickname);

    void remove(EntryImpl entry);

    default void create(Set<String> nicknames) {
        nicknames.forEach(this::create);
    }

    Set<EntryImpl> getAll();
}
