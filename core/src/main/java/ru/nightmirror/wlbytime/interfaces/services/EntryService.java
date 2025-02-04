package ru.nightmirror.wlbytime.interfaces.services;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.nightmirror.wlbytime.entry.EntryImpl;

import java.util.Set;

public interface EntryService {
    void remove(EntryImpl entry);

    @NotNull EntryImpl create(String nickname);

    @NotNull EntryImpl create(String nickname, long untilMs);

    void freeze(EntryImpl entry, long durationMs);

    void unfreeze(EntryImpl entry);

    @UnmodifiableView Set<EntryImpl> getEntries();
}
