package ru.nightmirror.wlbytime.impl.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.EntryImpl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccessEntryCheckerImplTest {

    private AccessEntryCheckerImpl checker;

    @BeforeEach
    public void setUp() {
        checker = new AccessEntryCheckerImpl();
    }

    @Test
    public void allowedWhenActiveAndNotFrozen() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(false);
        when(entry.isActive()).thenReturn(true);
        assertTrue(checker.isAllowed(entry));
    }

    @Test
    public void notAllowedWhenActiveAndFrozen() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(true);
        when(entry.isActive()).thenReturn(true);
        assertFalse(checker.isAllowed(entry));
    }

    @Test
    public void notAllowedWhenInactiveAndNotFrozen() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(false);
        when(entry.isActive()).thenReturn(false);
        assertFalse(checker.isAllowed(entry));
    }

    @Test
    public void notAllowedWhenInactiveAndFrozen() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(true);
        when(entry.isActive()).thenReturn(false);
        assertFalse(checker.isAllowed(entry));
    }
}
