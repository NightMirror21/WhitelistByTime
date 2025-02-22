package ru.nightmirror.wlbytime.impl.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import static org.mockito.Mockito.*;

public class UnfreezeEntryCheckerImplTest {

    private EntryService entryService;

    @BeforeEach
    public void setUp() {
        entryService = mock(EntryService.class);
    }

    @Test
    public void unfreezeCalledWhenUnfreezeEnabledAndEntryFrozen() {
        UnfreezeEntryCheckerImpl checker = new UnfreezeEntryCheckerImpl(true, entryService);
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(true);
        checker.unfreezeIfRequired(entry);
        verify(entryService).unfreeze(entry);
    }

    @Test
    public void unfreezeNotCalledWhenUnfreezeEnabledAndEntryNotFrozen() {
        UnfreezeEntryCheckerImpl checker = new UnfreezeEntryCheckerImpl(true, entryService);
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(false);
        checker.unfreezeIfRequired(entry);
        verify(entryService, never()).unfreeze(entry);
    }

    @Test
    public void unfreezeNotCalledWhenUnfreezeDisabledAndEntryFrozen() {
        UnfreezeEntryCheckerImpl checker = new UnfreezeEntryCheckerImpl(false, entryService);
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(true);
        checker.unfreezeIfRequired(entry);
        verify(entryService, never()).unfreeze(entry);
    }

    @Test
    public void unfreezeNotCalledWhenUnfreezeDisabledAndEntryNotFrozen() {
        UnfreezeEntryCheckerImpl checker = new UnfreezeEntryCheckerImpl(false, entryService);
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(false);
        checker.unfreezeIfRequired(entry);
        verify(entryService, never()).unfreeze(entry);
    }
}
