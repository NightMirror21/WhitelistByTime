package ru.nightmirror.wlbytime.impl.finder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.entry.WhitelistEntry;
import ru.nightmirror.wlbytime.interfaces.database.WhitelistEntryDao;
import ru.nightmirror.wlbytime.interfaces.finder.WhitelistEntryFinder;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WhitelistEntryFinderImpl implements WhitelistEntryFinder {

    boolean caseSensitive;
    WhitelistEntryDao dao;

    @Override
    public Optional<WhitelistEntry> find(String nickname) {
        if (caseSensitive) {
            return dao.get(nickname);
        } else {
            return dao.getLike(nickname);
        }
    }
}
