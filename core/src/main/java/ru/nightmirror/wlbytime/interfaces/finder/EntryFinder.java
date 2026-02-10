package ru.nightmirror.wlbytime.interfaces.finder;

import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.PlayerKey;

import java.util.Optional;

public interface EntryFinder {
    Optional<EntryImpl> find(String nickname);

    Optional<EntryImpl> find(PlayerKey key);

    default EntryImpl findOrThrow(String nickname) {
        return find(nickname).orElseThrow(() -> new NullPointerException(
                String.format("No whitelist entry found for nickname: %s", nickname)));
    }
}
