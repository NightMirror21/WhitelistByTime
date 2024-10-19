package ru.nightmirror.wlbytime.interfaces.database;

import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.entry.Entry;

import java.util.Optional;
import java.util.Set;

public interface EntryDao {
    void update(Entry entry);

    Optional<Entry> getLike(String nickname);

    Optional<Entry> get(String nickname);

    Entry create(String nickname, @Nullable Long milliseconds);

    default Entry create(String nickname) {
        return create(nickname, null);
    }

    default void create(Set<String> nicknames) {
        nicknames.forEach(this::create);
    }

    Set<Entry> getAll();
}
