package ru.nightmirror.wlbytime.interfaces.database;

import ru.nightmirror.wlbytime.entry.WhitelistEntry;

import java.util.Optional;

public interface WhitelistEntryDao {
    void update(WhitelistEntry entry);

    Optional<WhitelistEntry> getLike(String nickname);

    Optional<WhitelistEntry> get(String nickname);
}
