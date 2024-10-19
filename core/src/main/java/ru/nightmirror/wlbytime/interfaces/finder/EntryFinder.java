package ru.nightmirror.wlbytime.interfaces.finder;

import ru.nightmirror.wlbytime.entry.Entry;

import java.util.Optional;

public interface EntryFinder {
    Optional<Entry> find(String nickname);

    default Entry findOrThrow(String nickname) {
        return find(nickname).orElseThrow(() -> new NullPointerException(
                String.format("No whitelist entry found for nickname: %s", nickname)));
    }
}
