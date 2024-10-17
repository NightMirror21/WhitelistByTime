package ru.nightmirror.wlbytime.interfaces.checker;

import ru.nightmirror.wlbytime.entry.WhitelistEntry;

public interface UnfreezePlayerChecker {
    void unfreezeIfRequired(WhitelistEntry entry);
}
