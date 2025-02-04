package ru.nightmirror.wlbytime.impl.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import static org.mockito.Mockito.*;

public class UnfreezeEntryCheckerImplTest {

    private UnfreezeEntryCheckerImpl unfreezeEntryChecker;
    private EntryService entryService;

    @BeforeEach
    void setUp() {
        entryService = mock(EntryService.class);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsTrue_AndEntryIsCurrentlyFrozen_ShouldRemoveFreezeAndUpdate() {
        boolean unfreezeIfFrozen = true;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryService);

        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(true);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry, never()).unfreeze();
        verify(entryService).unfreeze(entry);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsTrue_AndEntryIsNotFrozen_ShouldNotRemoveFreezeOrUpdate() {
        boolean unfreezeIfFrozen = true;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryService);

        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(false);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry, never()).unfreeze();
        verify(entryService, never()).unfreeze(entry);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsFalse_AndEntryIsCurrentlyFrozen_ShouldNotRemoveFreezeOrUpdate() {
        boolean unfreezeIfFrozen = false;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryService);

        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(true);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry, never()).unfreeze();
        verify(entryService, never()).unfreeze(entry);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsFalse_AndEntryIsNotFrozen_ShouldNotRemoveFreezeOrUpdate() {
        boolean unfreezeIfFrozen = false;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryService);

        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(false);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry, never()).unfreeze();
        verify(entryService, never()).unfreeze(entry);
    }
}
