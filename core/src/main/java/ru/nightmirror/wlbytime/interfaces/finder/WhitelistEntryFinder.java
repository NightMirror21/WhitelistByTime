package ru.nightmirror.wlbytime.interfaces.finder;

import ru.nightmirror.wlbytime.entry.WhitelistEntry;

import java.util.Optional;

public interface WhitelistEntryFinder {
    Optional<WhitelistEntry> find(String nickname);

    default WhitelistEntry findOrThrow(String nickname) {
        return find(nickname).orElseThrow(() -> new NullPointerException(
                String.format("No whitelist entry found for nickname: %s", nickname)));
    }
}
