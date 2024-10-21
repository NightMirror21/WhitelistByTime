package ru.nightmirror.wlbytime.impl.finder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EntryFinderImpl implements EntryFinder {

    boolean caseSensitive;
    EntryDao dao;

    @Override
    public Optional<Entry> find(@NotNull String nickname) {
        if (nickname.isEmpty()) {
            return Optional.empty();
        }

        if (caseSensitive) {
            return dao.get(nickname);
        } else {
            return dao.getLike(nickname);
        }
    }
}
