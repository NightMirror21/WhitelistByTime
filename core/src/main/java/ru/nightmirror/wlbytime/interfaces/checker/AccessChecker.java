package ru.nightmirror.wlbytime.interfaces.checker;

import ru.nightmirror.wlbytime.entry.Entry;

public interface AccessChecker {
    boolean isAllowed(Entry entry);
}
