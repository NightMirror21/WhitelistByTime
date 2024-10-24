package ru.nightmirror.wlbytime.impl.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.entry.Entry;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntryDaoImplTest {

    private EntryDaoImpl entryDao;
    private DatabaseConfig config;

    @BeforeEach
    public void setUp() {
        config = new DatabaseConfig();
        config.setParams(new ArrayList<>());
        config.setName(":memory:");
        config.setType("sqlite");

        entryDao = new EntryDaoImpl(config);
    }

    @AfterEach
    public void tearDown() {
        entryDao.close();
    }

    @Test
    public void testCloseConnection() {
        assertDoesNotThrow(() -> entryDao.close());
    }

    @Test
    public void testCreateAndGetEntry() {
        long futureTime = System.currentTimeMillis() + 100000;
        Entry created = entryDao.create("testUser", futureTime);
        Optional<Entry> retrieved = entryDao.get("testUser");

        assertTrue(retrieved.isPresent());
        assertEquals(created.getNickname(), retrieved.get().getNickname());
        assertEquals(created.getUntilOrNull(), retrieved.get().getUntilOrNull());
    }

    @Test
    public void testUpdateEntry() {
        Entry created = entryDao.create("testUser", System.currentTimeMillis() + 100000);
        created.setNotInWhitelist();
        entryDao.update(created);

        Optional<Entry> updated = entryDao.get("testUser");
        assertTrue(updated.isPresent());
        assertTrue(updated.get().isNotInWhitelist());
    }

    @Test
    public void testGetLike() {
        entryDao.create("TESTUSER", System.currentTimeMillis() + 100000);
        entryDao.create("testuser", System.currentTimeMillis() + 100000);
        entryDao.create("anotherUser", System.currentTimeMillis() + 100000);

        Optional<Entry> retrieved = entryDao.getLike("testuser");

        assertTrue(retrieved.isPresent());
        String nickname = retrieved.get().getNickname();
        assertTrue(nickname.toLowerCase().contains("testuser"));
    }

    @Test
    public void testGetExact() {
        entryDao.create("ExactUser", System.currentTimeMillis() + 100000);

        Optional<Entry> retrieved = entryDao.get("ExactUser");
        Optional<Entry> notRetrieved = entryDao.get("exactuser");

        assertTrue(retrieved.isPresent());
        assertFalse(notRetrieved.isPresent());
    }

    @Test
    public void testGetAll() {
        entryDao.create("testUser1", System.currentTimeMillis() + 100000);
        entryDao.create("testUser2", System.currentTimeMillis() + 100000);
        entryDao.create("testUser3", System.currentTimeMillis() + 100000);

        Set<Entry> allEntries = entryDao.getAll();

        assertEquals(3, allEntries.size());
    }

    @Test
    public void testCreateEntryWithDefaultMilliseconds() {
        Entry created = entryDao.create("testUser", Entry.FOREVER);

        assertNotNull(created);
        assertEquals(Entry.FOREVER, created.getUntilOrNull());
    }

    @Test
    public void testGetEntryNotFound() {
        Optional<Entry> retrieved = entryDao.get("nonExistentUser");

        assertFalse(retrieved.isPresent());
    }

    @Test
    public void testGetLikeEntryNotFound() {
        Optional<Entry> retrieved = entryDao.getLike("nonExistent");

        assertFalse(retrieved.isPresent());
    }

    @Test
    public void testCreateEntryWithSQLException() {
        entryDao.close();

        Exception exception = assertThrows(EntryDaoImpl.DataAccessException.class, () ->
                entryDao.create("testUser", System.currentTimeMillis() + 100000)
        );

        assertTrue(exception.getMessage().contains("Failed to create entry"));
    }

    @Test
    public void testUpdateEntryWithSQLException() {
        Entry created = entryDao.create("testUser", System.currentTimeMillis() + 100000);
        entryDao.close();

        Exception exception = assertThrows(EntryDaoImpl.DataAccessException.class, () ->
                entryDao.update(created)
        );

        assertTrue(exception.getMessage().contains("Failed to update entry"));
    }

    @Test
    public void testGetAllWithSQLException() {
        entryDao.close();

        Exception exception = assertThrows(EntryDaoImpl.DataAccessException.class, () ->
                entryDao.getAll()
        );

        assertTrue(exception.getMessage().contains("Failed to retrieve all entries"));
    }

    @Test
    public void testReopenConnection() {
        entryDao.close();
        DatabaseConfig newConfig = new DatabaseConfig();
        newConfig.setParams(new ArrayList<>());
        newConfig.setName(":memory:");
        newConfig.setType("sqlite");

        assertDoesNotThrow(() -> entryDao.reopenConnection(newConfig));

        entryDao.create("reopenUser", System.currentTimeMillis() + 100000);
        Optional<Entry> retrieved = entryDao.get("reopenUser");

        assertTrue(retrieved.isPresent());
        assertEquals("reopenUser", retrieved.get().getNickname());
    }
}
