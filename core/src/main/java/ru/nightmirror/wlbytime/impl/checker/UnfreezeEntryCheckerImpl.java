package ru.nightmirror.wlbytime.impl.checker;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.checker.UnfreezeEntryChecker;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UnfreezeEntryCheckerImpl implements UnfreezeEntryChecker {

    boolean unfreezeIfFrozen;
    EntryDao dao;

    @Override
    public void unfreezeIfRequired(Entry entry) {
        if (unfreezeIfFrozen && entry.isFrozen()) {
            entry.unfreeze();
            dao.update(entry);
        }
    }
}
