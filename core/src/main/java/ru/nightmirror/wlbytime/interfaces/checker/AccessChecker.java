package ru.nightmirror.wlbytime.interfaces.checker;

import ru.nightmirror.wlbytime.entry.WhitelistEntry;

public interface AccessChecker {
    boolean isAllowed(WhitelistEntry entry);
}
