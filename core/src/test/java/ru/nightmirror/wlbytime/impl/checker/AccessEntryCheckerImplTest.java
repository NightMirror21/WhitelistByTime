package ru.nightmirror.wlbytime.impl.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.Entry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccessEntryCheckerImplTest {

    private AccessEntryCheckerImpl accessEntryChecker;

    @BeforeEach
    public void setUp() {
        accessEntryChecker = new AccessEntryCheckerImpl();
    }

    @Test
    public void testIsAllowed_WhenEntryIsCurrentlyActiveAndNotFrozen_ShouldReturnTrue() {
        Entry entry = mock(Entry.class);
        when(entry.isCurrentlyFrozen()).thenReturn(false);
        when(entry.isCurrentlyActive()).thenReturn(true);

        boolean result = accessEntryChecker.isAllowed(entry);

        assertTrue(result);
    }

    @Test
    public void testIsAllowed_WhenEntryIsCurrentlyActiveButFrozen_ShouldReturnFalse() {
        Entry entry = mock(Entry.class);
        when(entry.isCurrentlyFrozen()).thenReturn(true);
        when(entry.isCurrentlyActive()).thenReturn(true);

        boolean result = accessEntryChecker.isAllowed(entry);

        assertFalse(result);
    }

    @Test
    public void testIsAllowed_WhenEntryIsInactiveAndNotFrozen_ShouldReturnFalse() {
        Entry entry = mock(Entry.class);
        when(entry.isCurrentlyFrozen()).thenReturn(false);
        when(entry.isCurrentlyActive()).thenReturn(false);

        boolean result = accessEntryChecker.isAllowed(entry);

        assertFalse(result);
    }

    @Test
    public void testIsAllowed_WhenEntryIsInactiveAndFrozen_ShouldReturnFalse() {
        Entry entry = mock(Entry.class);
        when(entry.isCurrentlyFrozen()).thenReturn(true);
        when(entry.isCurrentlyActive()).thenReturn(false);

        boolean result = accessEntryChecker.isAllowed(entry);

        assertFalse(result);
    }
}
