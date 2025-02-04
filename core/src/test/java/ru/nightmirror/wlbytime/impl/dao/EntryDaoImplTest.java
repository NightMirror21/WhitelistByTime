package ru.nightmirror.wlbytime.impl.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.entry.Freezing;

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
    public void testUpdateEntry_WithFreezing_ShouldPersistFreezing() {
        EntryImpl entry = entryDao.create("freezing_test");
        Freezing freezing = new Freezing(entry.getId(), 10000L);
        entry.setFreezing(freezing);
        entryDao.update(entry);

        Optional<EntryImpl> retrievedEntry = entryDao.get("freezing_test");
        assertTrue(retrievedEntry.isPresent());
        assertNotNull(retrievedEntry.get().getFreezing());
        assertEquals(10000L, retrievedEntry.get().getFreezing().getDurationOfFreeze());
    }

    @Test
    public void testUpdateEntry_WithLastJoin_ShouldPersistLastJoin() {
        EntryImpl entry = entryDao.create("lastjoin_test");
        entry.updateLastJoin();
        entryDao.update(entry);

        Optional<EntryImpl> retrievedEntry = entryDao.get("lastjoin_test");
        assertTrue(retrievedEntry.isPresent());
        assertNotNull(retrievedEntry.get().getLastJoin());
        assertTrue(retrievedEntry.get().isJoined());
    }

    @Test
    public void testCreateEntry_DuplicateNickname_ShouldThrowException() {
        entryDao.create("duplicate_test");
        assertThrows(EntryDaoImpl.DataAccessException.class, () -> entryDao.create("duplicate_test"));
    }

    @Test
    public void testGetEntry_WithAllRelatedData_ShouldReturnCompleteEntry() {
        String nickname = "full_entry_test";
        EntryImpl entry = entryDao.create(nickname, System.currentTimeMillis() + 10000);
        entry.freeze(5000L);
        entry.updateLastJoin();
        entryDao.update(entry);

        Optional<EntryImpl> retrievedEntry = entryDao.get(nickname);
        assertTrue(retrievedEntry.isPresent());
        EntryImpl fullEntry = retrievedEntry.get();
        assertNotNull(fullEntry.getExpiration());
        assertNotNull(fullEntry.getFreezing());
        assertNotNull(fullEntry.getLastJoin());
    }

    @Test
    public void testCreateEntry_WithNickname_ShouldCreateEntryInDatabase() {
        String nickname = "test_nickname";
        EntryImpl entry = entryDao.create(nickname);

        assertNotNull(entry);
        assertEquals(nickname, entry.getNickname());

        Optional<EntryImpl> retrievedEntry = entryDao.get(nickname);
        assertTrue(retrievedEntry.isPresent());
        assertEquals(nickname, retrievedEntry.get().getNickname());
    }

    @Test
    public void testCreateEntry_WithExpirationTime_ShouldCreateEntryWithExpiration() {
        String nickname = "expiring_nickname";
        long expirationTime = System.currentTimeMillis() + 10000;

        EntryImpl entry = entryDao.create(nickname, expirationTime);

        assertNotNull(entry);
        assertEquals(nickname, entry.getNickname());
        assertNotNull(entry.getExpiration());
        assertEquals(new Timestamp(expirationTime), entry.getExpiration().getExpirationTime());
    }
    
    

    @Test
    public void testGetEntry_ByExistingNickname_ShouldReturnEntry() {
        String nickname = "existing_nickname";
        entryDao.create(nickname);

        Optional<EntryImpl> result = entryDao.get(nickname);

        assertTrue(result.isPresent());
        assertEquals(nickname, result.get().getNickname());
    }

    @Test
    public void testGetEntry_ByNonExistingNickname_ShouldReturnEmptyOptional() {
        Optional<EntryImpl> result = entryDao.get("non_existing_nickname");

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetEntryLike_BySimilarNickname_ShouldReturnMatchingEntry() {
        String nickname = "SIMILAR";
        entryDao.create(nickname);

        Optional<EntryImpl> result = entryDao.getLike("similar");

        assertTrue(result.isPresent());
        assertEquals(nickname, result.get().getNickname());
    }

    @Test
    public void testGetEntryLike_ByNoMatchingNickname_ShouldReturnEmptyOptional() {
        entryDao.create("some_nickname");

        Optional<EntryImpl> result = entryDao.getLike("unmatched");

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetAll_ShouldReturnAllEntries() {
        entryDao.create("first_nickname");
        entryDao.create("second_nickname");

        Set<EntryImpl> allEntries = entryDao.getAll();

        assertEquals(2, allEntries.size());
    }

    @Test
    public void testUpdateEntry_ShouldModifyExistingEntry() {
        EntryImpl entry = entryDao.create("updatable_nickname");
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000));
        entryDao.update(entry);

        Optional<EntryImpl> result = entryDao.get(entry.getNickname());

        assertTrue(result.isPresent());
        assertNotNull(result.get().getExpiration());
    }

    @Test
    public void testDeleteExpirationTableEntry_ShouldRemoveExpirationForEntry() {
        EntryImpl entry = entryDao.create("entry_with_expiration");
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000));
        entryDao.update(entry);

        entry.setForever();
        entryDao.update(entry);

        Optional<EntryImpl> result = entryDao.get(entry.getNickname());
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
        Set<EntryImpl> allEntries = entryDao.getAll();
        assertTrue(allEntries.isEmpty());
    }

    @Test
    public void testGetEntryLike_MultipleMatches_ShouldReturnFirstMatch() {
        entryDao.create("SiMiLaR");
        entryDao.create("SIMILAR");

        Optional<EntryImpl> result = entryDao.getLike("similar");
        assertTrue(result.isPresent());

    }

    @Test
    public void testUpdateEntry_TransactionRollbackOnFailure() {
        EntryImpl entry = entryDao.create("rollback_test");
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000));

        // Simulate a failure by causing an invalid operation
        entry.setNickname(null);

        assertThrows(EntryDaoImpl.DataAccessException.class, () -> entryDao.update(entry));

        Optional<EntryImpl> retrievedEntry = entryDao.get("rollback_test");
        assertTrue(retrievedEntry.isPresent());
        assertEquals("rollback_test", retrievedEntry.get().getNickname());
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

        EntryImpl entry = entryDao.create(nickname, expirationTime);

        assertNotNull(entry);
        assertNotNull(entry.getExpiration());
        assertEquals(new Timestamp(expirationTime), entry.getExpiration().getExpirationTime());

        Optional<EntryImpl> retrievedEntry = entryDao.get(nickname);
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
        EntryImpl entry = entryDao.create(nickname);

        entryDao.remove(entry);

        Optional<EntryImpl> result = entryDao.get(nickname);
        assertFalse(result.isPresent());
    }

    @Test
    public void testRemoveEntry_ShouldDeleteRelatedTables() {
        String nickname = "removable_with_related_data";
        long expirationTime = System.currentTimeMillis() + 10000;
        EntryImpl entry = entryDao.create(nickname, expirationTime);

        entryDao.remove(entry);

        Optional<EntryImpl> result = entryDao.get(nickname);
        assertFalse(result.isPresent());

        Set<EntryImpl> allEntries = entryDao.getAll();
        assertEquals(0, allEntries.size());
    }

    @Test
    public void testRemoveEntry_WhenEntryDoesNotExist_ShouldDoNothing() {
        EntryImpl nonExistingEntry = EntryImpl.builder().id(999L).nickname("non_existing").build();

        assertDoesNotThrow(() -> entryDao.remove(nonExistingEntry));
    }
}
