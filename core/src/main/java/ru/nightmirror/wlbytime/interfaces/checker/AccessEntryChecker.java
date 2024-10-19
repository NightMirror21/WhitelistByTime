package ru.nightmirror.wlbytime.interfaces.checker;

import ru.nightmirror.wlbytime.entry.Entry;

public interface AccessEntryChecker {
    boolean isAllowed(Entry entry);
}
