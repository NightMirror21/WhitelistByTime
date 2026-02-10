package ru.nightmirror.wlbytime.impl.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.PlayerKey;
import ru.nightmirror.wlbytime.identity.ResolvedPlayer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryIdentityService;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class EntryIdentityServiceImpl implements EntryIdentityService {

    EntryFinder finder;
    EntryService entryService;

    @Override
    public @NotNull Optional<EntryImpl> findOrMigrate(@NotNull ResolvedPlayer resolved, @NotNull String nicknameFallback) {
        Optional<EntryImpl> entry = finder.find(resolved.key());
        if (entry.isPresent() || resolved.uuid() == null) {
            return entry;
        }

        Optional<EntryImpl> legacy = finder.find(PlayerKey.nickname(nicknameFallback));
        legacy.ifPresent(found -> syncIdentity(found, resolved));
        return legacy;
    }

    private void syncIdentity(@NotNull EntryImpl entry, @NotNull ResolvedPlayer resolved) {
        boolean changed = false;
        String resolvedUuid = resolved.uuid() != null ? resolved.uuid().toString() : null;
        if (resolvedUuid != null && (entry.getUuid() == null || !entry.getUuid().equalsIgnoreCase(resolvedUuid))) {
            entry.setUuid(resolvedUuid);
            changed = true;
        }
        if (!entry.getNickname().equals(resolved.nickname())) {
            entry.setNickname(resolved.nickname());
            changed = true;
        }
        if (changed) {
            log.info("Migrating identity for entryId={} nickname={} uuid={}", entry.getId(), entry.getNickname(), entry.getUuid());
            entryService.update(entry);
        }
    }
}
