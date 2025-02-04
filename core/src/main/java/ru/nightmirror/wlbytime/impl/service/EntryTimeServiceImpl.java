package ru.nightmirror.wlbytime.impl.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EntryTimeServiceImpl implements EntryTimeService {

    EntryDao entryDao;

    @Override
    public void add(EntryImpl entry, long milliseconds) {
        if (entry.isForever()) {
            throw new IllegalArgumentException("Entry is forever");
        }

        entry.getExpiration().add(milliseconds);
        entryDao.update(entry);
    }

    @Override
    public boolean canAdd(EntryImpl entry, long milliseconds) {
        return !entry.isForever() && entry.getExpiration().canAdd(milliseconds);
    }

    @Override
    public void remove(EntryImpl entry, long milliseconds) {
        if (entry.isForever()) {
            throw new IllegalArgumentException("Entry is forever");
        }
        entry.getExpiration().remove(milliseconds);
        entryDao.update(entry);
    }

    @Override
    public boolean canRemove(EntryImpl entry, long milliseconds) {
        return !entry.isForever() && entry.getExpiration().canRemove(milliseconds);
    }

    @Override
    public void set(EntryImpl entry, long milliseconds) {
        entry.getExpiration().set(milliseconds);
        entryDao.update(entry);
    }
}
