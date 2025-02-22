package ru.nightmirror.wlbytime.interfaces.services;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.nightmirror.wlbytime.entry.EntryImpl;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public interface EntryService {
    void remove(EntryImpl entry);

    @NotNull EntryImpl create(String nickname);

    @NotNull EntryImpl create(String nickname, Instant until);

    void freeze(EntryImpl entry, Duration duration);

    void unfreeze(EntryImpl entry);

    @UnmodifiableView Set<EntryImpl> getEntries();
}
