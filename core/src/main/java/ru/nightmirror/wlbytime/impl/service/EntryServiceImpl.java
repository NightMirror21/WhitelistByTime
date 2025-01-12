package ru.nightmirror.wlbytime.impl.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import java.util.Collections;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EntryServiceImpl implements EntryService {

    EntryDao entryDao;

    @Override
    public void remove(Entry entry) {
        entryDao.remove(entry);
    }

    @Override
    public @NotNull Entry create(String nickname) {
        return entryDao.create(nickname);
    }

    @Override
    public @NotNull Entry create(String nickname, long untilMs) {
        if (untilMs <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Until must be in the future");
        }

        return entryDao.create(nickname, untilMs);
    }

    @Override
    public void freeze(Entry entry, long durationMs) {
        if (durationMs <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }

        entry.freeze(durationMs);
        entryDao.update(entry);
    }

    @Override
    public void unfreeze(Entry entry) {
        entry.unfreeze();
        entryDao.update(entry);
    }

    @Override
    public @UnmodifiableView Set<Entry> getEntries() {
        return Collections.unmodifiableSet(entryDao.getAll());
    }
}
