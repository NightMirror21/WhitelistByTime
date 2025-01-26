package ru.nightmirror.wlbytime.impl.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.entry.Entry;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntryDaoImplTest {

    private EntryDaoImpl entryDao;
    private DatabaseConfig databaseConfig;

    @BeforeEach
    public void setUp() {
        databaseConfig = mock(DatabaseConfig.class);
        when(databaseConfig.getType()).thenReturn("sqlite");
        when(databaseConfig.getName()).thenReturn(":memory:");
        entryDao = new EntryDaoImpl(databaseConfig);
    }

    @AfterEach
    public void tearDown() {
        entryDao.close();
    }

    @Test
    public void testInitialization_WithValidConfig_ShouldInitializeDao() {
        assertNotNull(entryDao);
    }

    @Test
    public void testInitialization_WithInvalidDatabaseType_ShouldThrowException() {
        when(databaseConfig.getType()).thenReturn("unsupported_db_type");
        assertThrows(EntryDaoImpl.UnsupportedDatabaseTypeException.class, () -> new EntryDaoImpl(databaseConfig));
    }

    @Test
    public void testReopenConnection_WithNewConfig_ShouldCloseOldConnectionAndReinitialize() {
        DatabaseConfig newConfig = mock(DatabaseConfig.class);
        when(newConfig.getType()).thenReturn("sqlite");
        when(newConfig.getName()).thenReturn(":memory:");

        assertDoesNotThrow(() -> entryDao.reopenConnection(newConfig));

        assertNotNull(entryDao);
    }

    @Test
    public void testCreateEntry_WithNickname_ShouldCreateEntryInDatabase() {
        String nickname = "test_nickname";
        Entry entry = entryDao.create(nickname);

        assertNotNull(entry);
        assertEquals(nickname, entry.getNickname());

        Optional<Entry> retrievedEntry = entryDao.get(nickname);
        assertTrue(retrievedEntry.isPresent());
        assertEquals(nickname, retrievedEntry.get().getNickname());
    }

    @Test
    public void testCreateEntry_WithExpirationTime_ShouldCreateEntryWithExpiration() {
        String nickname = "expiring_nickname";
        long expirationTime = System.currentTimeMillis() + 10000;

        Entry entry = entryDao.create(nickname, expirationTime);

        assertNotNull(entry);
        assertEquals(nickname, entry.getNickname());
        assertNotNull(entry.getExpiration());
        assertEquals(new Timestamp(expirationTime), entry.getExpiration().getExpirationTime());
    }
    
    

    @Test
    public void testGetEntry_ByExistingNickname_ShouldReturnEntry() {
        String nickname = "existing_nickname";
        entryDao.create(nickname);

        Optional<Entry> result = entryDao.get(nickname);

        assertTrue(result.isPresent());
        assertEquals(nickname, result.get().getNickname());
    }

    @Test
    public void testGetEntry_ByNonExistingNickname_ShouldReturnEmptyOptional() {
        Optional<Entry> result = entryDao.get("non_existing_nickname");

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetEntryLike_BySimilarNickname_ShouldReturnMatchingEntry() {
        String nickname = "similar_name";
        entryDao.create(nickname);

        Optional<Entry> result = entryDao.getLike("similar");

        assertTrue(result.isPresent());
        assertEquals(nickname, result.get().getNickname());
    }

    @Test
    public void testGetEntryLike_ByNoMatchingNickname_ShouldReturnEmptyOptional() {
        entryDao.create("some_nickname");

        Optional<Entry> result = entryDao.getLike("unmatched");

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetAll_ShouldReturnAllEntries() {
        entryDao.create("first_nickname");
        entryDao.create("second_nickname");

        Set<Entry> allEntries = entryDao.getAll();

        assertEquals(2, allEntries.size());
    }

    @Test
    public void testUpdateEntry_ShouldModifyExistingEntry() {
        Entry entry = entryDao.create("updatable_nickname");
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000));
        entryDao.update(entry);

        Optional<Entry> result = entryDao.get(entry.getNickname());

        assertTrue(result.isPresent());
        assertNotNull(result.get().getExpiration());
    }

    @Test
    public void testDeleteExpirationTableEntry_ShouldRemoveExpirationForEntry() {
        Entry entry = entryDao.create("entry_with_expiration");
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000));
        entryDao.update(entry);

        entry.setForever();
        entryDao.update(entry);

        Optional<Entry> result = entryDao.get(entry.getNickname());
        assertTrue(result.isPresent());
        assertNull(result.get().getExpiration());
    }

    @Test
    public void testClose_WithOpenConnection_ShouldCloseWithoutError() {
        assertDoesNotThrow(() -> entryDao.close());
    }

    @Test
    public void testInitialization_WithUnsupportedDatabaseType_ShouldThrowUnsupportedDatabaseTypeException() {
        when(databaseConfig.getType()).thenReturn("unknown_db");
        assertThrows(EntryDaoImpl.UnsupportedDatabaseTypeException.class, () -> new EntryDaoImpl(databaseConfig));
    }

    @Test
    public void testReopenConnection_WithSameConfig_ShouldNotThrowErrors() {
        assertDoesNotThrow(() -> entryDao.reopenConnection(databaseConfig));
    }

    @Test
    public void testGetAll_WithEmptyDatabase_ShouldReturnEmptySet() {
        Set<Entry> allEntries = entryDao.getAll();
        assertTrue(allEntries.isEmpty());
    }

    @Test
    public void testGetEntryLike_MultipleMatches_ShouldReturnFirstMatch() {
        entryDao.create("similar_entry_one");
        entryDao.create("similar_entry_two");

        Optional<Entry> result = entryDao.getLike("similar_entry");
        assertTrue(result.isPresent());
        assertTrue(result.get().getNickname().contains("similar_entry"));
    }

    @Test
    public void testGetEntry_WithPartialData_ShouldReturnEntry() {
        String nickname = "partial_data_entry";
        entryDao.create(nickname);
        entryDao.create(nickname);

        Optional<Entry> result = entryDao.get(nickname);
        assertTrue(result.isPresent());
        assertEquals(nickname, result.get().getNickname());
    }

    @Test
    public void testInitConnection_ShouldCreateRequiredTables() {
        DatabaseConfig newConfig = mock(DatabaseConfig.class);
        when(newConfig.getType()).thenReturn("sqlite");
        when(newConfig.getName()).thenReturn(":memory:");

        EntryDaoImpl dao = new EntryDaoImpl(newConfig);
        assertNotNull(dao);
    }

    @Test
    public void testCreateEntry_WithExpiration_ShouldPersistExpiration() {
        String nickname = "expiring_entry";
        long expirationTime = System.currentTimeMillis() + 10000;

        Entry entry = entryDao.create(nickname, expirationTime);

        assertNotNull(entry);
        assertNotNull(entry.getExpiration());
        assertEquals(new Timestamp(expirationTime), entry.getExpiration().getExpirationTime());

        Optional<Entry> retrievedEntry = entryDao.get(nickname);
        assertTrue(retrievedEntry.isPresent());
        assertNotNull(retrievedEntry.get().getExpiration());
    }

    @Test
    public void testClose_MultipleTimes_ShouldNotThrowError() {
        assertDoesNotThrow(() -> entryDao.close());
        assertDoesNotThrow(() -> entryDao.close());
    }

    @Test
    public void testRemoveEntry_ShouldDeleteEntryFromDatabase() {
        String nickname = "removable_nickname";
        Entry entry = entryDao.create(nickname);

        entryDao.remove(entry);

        Optional<Entry> result = entryDao.get(nickname);
        assertFalse(result.isPresent());
    }

    @Test
    public void testRemoveEntry_ShouldDeleteRelatedTables() {
        String nickname = "removable_with_related_data";
        long expirationTime = System.currentTimeMillis() + 10000;
        Entry entry = entryDao.create(nickname, expirationTime);

        entryDao.remove(entry);

        Optional<Entry> result = entryDao.get(nickname);
        assertFalse(result.isPresent());

        Set<Entry> allEntries = entryDao.getAll();
        assertEquals(0, allEntries.size());
    }

    @Test
    public void testRemoveEntry_WhenEntryDoesNotExist_ShouldDoNothing() {
        Entry nonExistingEntry = Entry.builder().id(999L).nickname("non_existing").build();

        assertDoesNotThrow(() -> entryDao.remove(nonExistingEntry));
    }
}
