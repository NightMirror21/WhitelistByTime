package ru.nightmirror.wlbytime.impl.finder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.PlayerKey;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EntryFinderImpl implements EntryFinder {

    boolean caseSensitive;
    EntryDao dao;

    @Override
    public Optional<EntryImpl> find(@NotNull String nickname) {
        return find(PlayerKey.nickname(nickname));
    }

    @Override
    public Optional<EntryImpl> find(@NotNull PlayerKey key) {
        if (key.value().isEmpty()) {
            return Optional.empty();
        }

        if (key.uuid()) {
            return dao.getByUuid(key.value());
        }

        if (caseSensitive) {
            return dao.get(key.value());
        } else {
            return dao.getLike(key.value());
        }
    }
}
