package ru.nightmirror.wlbytime.interfaces.services;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.nightmirror.wlbytime.entry.Entry;

import java.util.Set;

public interface EntryService {
    void remove(Entry entry);

    @NotNull Entry create(String nickname);

    @NotNull Entry create(String nickname, long untilMs);

    void freeze(Entry entry, long durationMs);

    void unfreeze(Entry entry);

    @UnmodifiableView Set<Entry> getEntries();
}
