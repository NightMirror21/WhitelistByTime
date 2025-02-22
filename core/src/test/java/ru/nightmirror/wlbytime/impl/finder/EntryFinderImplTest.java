package ru.nightmirror.wlbytime.impl.finder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class EntryFinderImplTest {

    private EntryDao entryDao;

    @BeforeEach
    public void setUp() {
        entryDao = mock(EntryDao.class);
    }

    @Test
    public void findEmptyNicknameReturnsEmpty() {
        EntryFinderImpl finder = new EntryFinderImpl(true, entryDao);
        Optional<EntryImpl> result = finder.find("");
        assertTrue(result.isEmpty());
        verifyNoInteractions(entryDao);
    }

    @Test
    public void findCaseSensitiveFound() {
        EntryFinderImpl finder = new EntryFinderImpl(true, entryDao);
        String nickname = "User123";
        EntryImpl entry = mock(EntryImpl.class);
        when(entryDao.get(nickname)).thenReturn(Optional.of(entry));
        Optional<EntryImpl> result = finder.find(nickname);
        assertTrue(result.isPresent());
        assertEquals(entry, result.get());
        verify(entryDao).get(nickname);
        verify(entryDao, never()).getLike(anyString());
    }

    @Test
    public void findCaseSensitiveNotFound() {
        EntryFinderImpl finder = new EntryFinderImpl(true, entryDao);
        String nickname = "User123";
        when(entryDao.get(nickname)).thenReturn(Optional.empty());
        Optional<EntryImpl> result = finder.find(nickname);
        assertTrue(result.isEmpty());
        verify(entryDao).get(nickname);
        verify(entryDao, never()).getLike(anyString());
    }

    @Test
    public void findCaseInsensitiveFound() {
        EntryFinderImpl finder = new EntryFinderImpl(false, entryDao);
        String nickname = "User123";
        EntryImpl entry = mock(EntryImpl.class);
        when(entryDao.getLike(nickname)).thenReturn(Optional.of(entry));
        Optional<EntryImpl> result = finder.find(nickname);
        assertTrue(result.isPresent());
        assertEquals(entry, result.get());
        verify(entryDao).getLike(nickname);
        verify(entryDao, never()).get(anyString());
    }

    @Test
    public void findCaseInsensitiveNotFound() {
        EntryFinderImpl finder = new EntryFinderImpl(false, entryDao);
        String nickname = "User123";
        when(entryDao.getLike(nickname)).thenReturn(Optional.empty());
        Optional<EntryImpl> result = finder.find(nickname);
        assertTrue(result.isEmpty());
        verify(entryDao).getLike(nickname);
        verify(entryDao, never()).get(anyString());
    }
}
