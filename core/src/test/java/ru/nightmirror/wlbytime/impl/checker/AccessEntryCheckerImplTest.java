package ru.nightmirror.wlbytime.impl.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.EntryImpl;

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
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(false);
        when(entry.isActive()).thenReturn(true);

        boolean result = accessEntryChecker.isAllowed(entry);

        assertTrue(result);
    }

    @Test
    public void testIsAllowed_WhenEntryIsCurrentlyActiveButFrozen_ShouldReturnFalse() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(true);
        when(entry.isActive()).thenReturn(true);

        boolean result = accessEntryChecker.isAllowed(entry);

        assertFalse(result);
    }

    @Test
    public void testIsAllowed_WhenEntryIsInactiveAndNotFrozen_ShouldReturnFalse() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(false);
        when(entry.isActive()).thenReturn(false);

        boolean result = accessEntryChecker.isAllowed(entry);

        assertFalse(result);
    }

    @Test
    public void testIsAllowed_WhenEntryIsInactiveAndFrozen_ShouldReturnFalse() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(true);
        when(entry.isActive()).thenReturn(false);

        boolean result = accessEntryChecker.isAllowed(entry);

        assertFalse(result);
    }
}
