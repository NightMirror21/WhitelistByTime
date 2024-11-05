package ru.nightmirror.wlbytime.impl.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

import static org.mockito.Mockito.*;

public class UnfreezeEntryCheckerImplTest {

    private UnfreezeEntryCheckerImpl unfreezeEntryChecker;
    private EntryDao entryDao;

    @BeforeEach
    void setUp() {
        entryDao = mock(EntryDao.class);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsTrue_AndEntryIsCurrentlyFrozen_ShouldRemoveFreezeAndUpdate() {
        boolean unfreezeIfFrozen = true;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryDao);

        Entry entry = mock(Entry.class);
        when(entry.isCurrentlyFrozen()).thenReturn(true);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry).removeFreeze();
        verify(entryDao).update(entry);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsTrue_AndEntryIsNotFrozen_ShouldNotRemoveFreezeOrUpdate() {
        boolean unfreezeIfFrozen = true;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryDao);

        Entry entry = mock(Entry.class);
        when(entry.isCurrentlyFrozen()).thenReturn(false);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry, never()).removeFreeze();
        verify(entryDao, never()).update(entry);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsFalse_AndEntryIsCurrentlyFrozen_ShouldNotRemoveFreezeOrUpdate() {
        boolean unfreezeIfFrozen = false;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryDao);

        Entry entry = mock(Entry.class);
        when(entry.isCurrentlyFrozen()).thenReturn(true);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry, never()).removeFreeze();
        verify(entryDao, never()).update(entry);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsFalse_AndEntryIsNotFrozen_ShouldNotRemoveFreezeOrUpdate() {
        boolean unfreezeIfFrozen = false;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryDao);

        Entry entry = mock(Entry.class);
        when(entry.isCurrentlyFrozen()).thenReturn(false);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry, never()).removeFreeze();
        verify(entryDao, never()).update(entry);
    }
}
