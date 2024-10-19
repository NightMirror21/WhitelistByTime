package ru.nightmirror.wlbytime.interfaces.checker;

import ru.nightmirror.wlbytime.entry.Entry;

public interface UnfreezeEntryChecker {
    void unfreezeIfRequired(Entry entry);
}
