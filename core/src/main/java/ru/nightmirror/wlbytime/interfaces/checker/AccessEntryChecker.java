package ru.nightmirror.wlbytime.interfaces.checker;

import ru.nightmirror.wlbytime.entry.EntryImpl;

public interface AccessEntryChecker {
    boolean isAllowed(EntryImpl entry);
}
