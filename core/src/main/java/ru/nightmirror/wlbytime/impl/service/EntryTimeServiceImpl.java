package ru.nightmirror.wlbytime.impl.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;

import java.time.Duration;
import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EntryTimeServiceImpl implements EntryTimeService {

    EntryDao entryDao;

    @Override
    public void add(EntryImpl entry, Duration duration) {
        if (entry.isForever()) {
            throw new IllegalArgumentException("Entry is forever");
        }

        entry.getExpiration().add(duration);
        entryDao.update(entry);
    }

    @Override
    public boolean canAdd(EntryImpl entry, Duration duration) {
        return !entry.isForever() && entry.getExpiration().canAdd(duration);
    }

    @Override
    public void remove(EntryImpl entry, Duration duration) {
        if (entry.isForever()) {
            throw new IllegalArgumentException("Entry is forever");
        }
        entry.getExpiration().remove(duration);
        entryDao.update(entry);
    }

    @Override
    public boolean canRemove(EntryImpl entry, Duration duration) {
        return !entry.isForever() && entry.getExpiration().canRemove(duration);
    }

    @Override
    public void set(EntryImpl entry, Instant instant) {
        entry.getExpiration().set(instant);
        entryDao.update(entry);
    }
}
