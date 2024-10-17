package ru.nightmirror.wlbytime.impl.checker;

import ru.nightmirror.wlbytime.entry.WhitelistEntry;
import ru.nightmirror.wlbytime.interfaces.checker.AccessChecker;

public class AccessCheckerImpl implements AccessChecker {

    @Override
    public boolean isAllowed(WhitelistEntry entry) {
        if (entry.isFrozen()) {
            return false;
        }
        return entry.isActive();
    }
}
