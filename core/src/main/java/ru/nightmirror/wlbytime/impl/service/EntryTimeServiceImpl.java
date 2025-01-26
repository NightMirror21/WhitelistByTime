package ru.nightmirror.wlbytime.impl.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EntryTimeServiceImpl implements EntryTimeService {

    EntryDao entryDao;

    @Override
    public void add(Entry entry, long milliseconds) {
        if (entry.isForever()) {
            throw new IllegalArgumentException("Entry is forever");
        }

        entry.getExpiration().add(milliseconds);
        entryDao.update(entry);
    }

    @Override
    public boolean canAdd(Entry entry, long milliseconds) {
        return !entry.isForever() && entry.getExpiration().canAdd(milliseconds);
    }

    @Override
    public void remove(Entry entry, long milliseconds) {
        if (entry.isForever()) {
            throw new IllegalArgumentException("Entry is forever");
        }
        entry.getExpiration().remove(milliseconds);
        entryDao.update(entry);
    }

    @Override
    public boolean canRemove(Entry entry, long milliseconds) {
        return !entry.isForever() && entry.getExpiration().canRemove(milliseconds);
    }

    @Override
    public void set(Entry entry, long milliseconds) {
        entry.getExpiration().set(milliseconds);
        entryDao.update(entry);
    }
}
