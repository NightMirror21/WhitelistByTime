package ru.nightmirror.wlbytime.impl.checker;

import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.checker.AccessEntryChecker;

public class AccessEntryCheckerImpl implements AccessEntryChecker {

    @Override
    public boolean isAllowed(Entry entry) {
        if (entry.isFreezeActive()) {
            return false;
        }
        return entry.isActive();
    }
}
