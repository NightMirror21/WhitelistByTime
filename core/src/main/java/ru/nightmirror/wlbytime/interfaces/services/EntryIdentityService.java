package ru.nightmirror.wlbytime.interfaces.services;

import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.ResolvedPlayer;

import java.util.Optional;

public interface EntryIdentityService {
    @NotNull Optional<EntryImpl> findOrMigrate(@NotNull ResolvedPlayer resolved, @NotNull String nicknameFallback);
}
