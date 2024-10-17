package ru.nightmirror.wlbytime.impl.checker;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.entry.WhitelistEntry;
import ru.nightmirror.wlbytime.interfaces.checker.UnfreezePlayerChecker;
import ru.nightmirror.wlbytime.interfaces.database.WhitelistEntryDao;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UnfreezePlayerCheckerImpl implements UnfreezePlayerChecker {

    boolean unfreezeIfFrozen;
    WhitelistEntryDao dao;

    @Override
    public void unfreezeIfRequired(WhitelistEntry entry) {
        if (unfreezeIfFrozen && entry.isFrozen()) {
            entry.unfreeze();
            dao.update(entry);
        }
    }
}
