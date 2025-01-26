package ru.nightmirror.wlbytime.impl.checker;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.checker.UnfreezeEntryChecker;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UnfreezeEntryCheckerImpl implements UnfreezeEntryChecker {

    boolean unfreezeIfFrozen;
    EntryService entryService;

    @Override
    public void unfreezeIfRequired(Entry entry) {
        if (unfreezeIfFrozen && entry.isFreezeActive()) {
            entryService.unfreeze(entry);
        }
    }
}
