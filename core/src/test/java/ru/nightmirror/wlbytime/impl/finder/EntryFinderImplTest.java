package ru.nightmirror.wlbytime.impl.finder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EntryFinderImplTest {

    private EntryFinderImpl entryFinder;
    private EntryDao entryDao;

    @BeforeEach
    public void setUp() {
        entryDao = mock(EntryDao.class);
    }

    @Test
    public void testFind_WhenCaseSensitiveIsTrue_ShouldCallGetMethod() {
        boolean caseSensitive = true;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "User123";
        Optional<Entry> expectedEntry = Optional.of(mock(Entry.class));

        when(entryDao.get(nickname)).thenReturn(expectedEntry);

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).get(nickname);
        verify(entryDao, never()).getLike(anyString());
        assertEquals(expectedEntry, result);
    }

    @Test
    public void testFind_WhenCaseSensitiveIsFalse_ShouldCallGetLikeMethod() {
        boolean caseSensitive = false;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "User123";
        Optional<Entry> expectedEntry = Optional.of(mock(Entry.class));

        when(entryDao.getLike(nickname)).thenReturn(expectedEntry);

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).getLike(nickname);
        verify(entryDao, never()).get(anyString());
        assertEquals(expectedEntry, result);
    }

    @Test
    public void testFind_WhenCaseSensitiveIsTrue_AndEntryNotFound_ShouldReturnEmptyOptional() {
        boolean caseSensitive = true;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "NonExistentUser";
        when(entryDao.get(nickname)).thenReturn(Optional.empty());

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).get(nickname);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFind_WhenCaseSensitiveIsFalse_AndEntryNotFound_ShouldReturnEmptyOptional() {
        boolean caseSensitive = false;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "NonExistentUser";
        when(entryDao.getLike(nickname)).thenReturn(Optional.empty());

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).getLike(nickname);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFind_WhenCaseSensitiveIsTrue_WithDifferentCase_ShouldNotFindEntry() {
        boolean caseSensitive = true;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "user123";
        when(entryDao.get(nickname)).thenReturn(Optional.empty());

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).get(nickname);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFind_WhenCaseSensitiveIsFalse_WithDifferentCase_ShouldFindEntry() {
        boolean caseSensitive = false;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "user123";
        Optional<Entry> expectedEntry = Optional.of(mock(Entry.class));

        when(entryDao.getLike(nickname)).thenReturn(expectedEntry);

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).getLike(nickname);
        verify(entryDao, never()).get(anyString());
        assertEquals(expectedEntry, result);
    }

    @Test
    public void testFind_WhenCaseSensitiveIsTrue_WithSpecialCharacters_ShouldCallGetMethod() {
        boolean caseSensitive = true;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "User@123!";
        Optional<Entry> expectedEntry = Optional.of(mock(Entry.class));

        when(entryDao.get(nickname)).thenReturn(expectedEntry);

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).get(nickname);
        assertEquals(expectedEntry, result);
    }

    @Test
    public void testFind_WhenCaseSensitiveIsFalse_WithSpecialCharacters_ShouldCallGetLikeMethod() {
        boolean caseSensitive = false;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "User@123!";
        Optional<Entry> expectedEntry = Optional.of(mock(Entry.class));

        when(entryDao.getLike(nickname)).thenReturn(expectedEntry);

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).getLike(nickname);
        assertEquals(expectedEntry, result);
    }

    @Test
    public void testFind_WhenCaseSensitiveIsFalse_WithEmptyNickname_ShouldReturnEmptyOptional() {
        boolean caseSensitive = false;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        Optional<Entry> result = entryFinder.find("");

        assertTrue(result.isEmpty());
        verify(entryDao, never()).getLike(anyString());
    }

    @Test
    public void testFind_WhenCaseSensitiveIsTrue_WithMixedCaseUnicode_ShouldHandleProperly() {
        boolean caseSensitive = true;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "UsérÜniçødë";
        Optional<Entry> expectedEntry = Optional.of(mock(Entry.class));

        when(entryDao.get(nickname)).thenReturn(expectedEntry);

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).get(nickname);
        assertEquals(expectedEntry, result);
    }

    @Test
    public void testFind_WhenCaseSensitiveIsFalse_WithMixedCaseUnicode_ShouldCallGetLike() {
        boolean caseSensitive = false;
        entryFinder = new EntryFinderImpl(caseSensitive, entryDao);

        String nickname = "UsérÜniçødë";
        Optional<Entry> expectedEntry = Optional.of(mock(Entry.class));

        when(entryDao.getLike(nickname)).thenReturn(expectedEntry);

        Optional<Entry> result = entryFinder.find(nickname);

        verify(entryDao).getLike(nickname);
        assertEquals(expectedEntry, result);
    }
}
