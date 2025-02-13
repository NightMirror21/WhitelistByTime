package ru.nightmirror.wlbytime.impl.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EntryServiceImpl implements EntryService {

    EntryDao entryDao;

    @Override
    public void remove(EntryImpl entry) {
        entryDao.remove(entry);
    }

    @Override
    public @NotNull EntryImpl create(String nickname) {
        return entryDao.create(nickname);
    }

    @Override
    public @NotNull EntryImpl create(String nickname, Instant until) {
        if (until.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Until must be in the future");
        }

        return entryDao.create(nickname, until);
    }

    @Override
    public void freeze(EntryImpl entry, Duration duration) {
        entry.freeze(duration);
        entryDao.update(entry);
    }

    @Override
    public void unfreeze(EntryImpl entry) {
        entry.unfreeze();
        entryDao.update(entry);
    }

    @Override
    public @UnmodifiableView Set<EntryImpl> getEntries() {
        return Collections.unmodifiableSet(entryDao.getAll());
    }
}
