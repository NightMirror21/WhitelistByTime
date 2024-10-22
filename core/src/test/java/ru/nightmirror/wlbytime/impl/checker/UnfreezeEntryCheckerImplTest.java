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
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsTrue_AndEntryIsFrozen_ShouldUnfreezeAndUpdate() {
        boolean unfreezeIfFrozen = true;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryDao);

        Entry entry = mock(Entry.class);
        when(entry.isFrozen()).thenReturn(true);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry).unfreeze();
        verify(entryDao).update(entry);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsTrue_AndEntryIsNotFrozen_ShouldNotUnfreezeOrUpdate() {
        boolean unfreezeIfFrozen = true;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryDao);

        Entry entry = mock(Entry.class);
        when(entry.isFrozen()).thenReturn(false);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry, never()).unfreeze();
        verify(entryDao, never()).update(entry);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsFalse_AndEntryIsFrozen_ShouldNotUnfreezeOrUpdate() {
        boolean unfreezeIfFrozen = false;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryDao);

        Entry entry = mock(Entry.class);
        when(entry.isFrozen()).thenReturn(true);

        unfreezeEntryChecker.unfreezeIfRequired(entry);

        verify(entry, never()).unfreeze();
        verify(entryDao, never()).update(entry);
    }

    @Test
    public void testUnfreezeIfRequired_WhenUnfreezeIfFrozenIsFalse_AndEntryIsNotFrozen_ShouldNotUnfreezeOrUpdate() {
        // Arrange
        boolean unfreezeIfFrozen = false;
        unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(unfreezeIfFrozen, entryDao);

        Entry entry = mock(Entry.class);
        when(entry.isFrozen()).thenReturn(false);

        // Act
        unfreezeEntryChecker.unfreezeIfRequired(entry);

        // Assert
        verify(entry, never()).unfreeze();
        verify(entryDao, never()).update(entry);
    }
}