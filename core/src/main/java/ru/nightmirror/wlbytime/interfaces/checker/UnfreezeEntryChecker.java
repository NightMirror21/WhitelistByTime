package ru.nightmirror.wlbytime.interfaces.checker;

import ru.nightmirror.wlbytime.entry.EntryImpl;

public interface UnfreezeEntryChecker {
    void unfreezeIfRequired(EntryImpl entry);
}
