package ru.nightmirror.wlbytime.impl.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.entry.Entry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntryDaoImplTest {

    private EntryDaoImpl entryDao;
    private DatabaseConfig config;

    @BeforeEach
    public void setUp() throws SQLException {
        config = new DatabaseConfig();
        config.setParams(new ArrayList<>());
        config.setName(":memory:");
        config.setType("sqlite");

        entryDao = new EntryDaoImpl(config);
    }

    @AfterEach
    public void tearDown() {
        entryDao.closeConnection();
    }

    @Test
    public void testCloseConnection() {
        entryDao.closeConnection();

        assertDoesNotThrow(() -> entryDao.closeConnection());
    }

    @Test
    public void testCreateAndGetEntry() {
        Entry created = entryDao.create("testUser", System.currentTimeMillis() + 100000);
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

        Optional<Entry> retrieved = entryDao.getLike("testuser");

        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get().getNickname().contains("testuser") || retrieved.get().getNickname().contains("TESTUSER"));
    }

    @Test
    public void testGetNotLike() {
        entryDao.create("TESTUSER", System.currentTimeMillis() + 100000);
        entryDao.create("testuser", System.currentTimeMillis() + 100000);

        Optional<Entry> retrieved1 = entryDao.get("TESTUSER");
        Optional<Entry> retrieved2 = entryDao.get("testuser");

        assertTrue(retrieved1.isPresent());
        assertTrue(retrieved2.isPresent());
        assertTrue(retrieved1.get().getNickname().contains("TESTUSER"));
        assertTrue(retrieved2.get().getNickname().contains("testuser"));
    }

    @Test
    public void testGetAll() {
        entryDao.create("testUser1", System.currentTimeMillis() + 100000);
        entryDao.create("testUser2", System.currentTimeMillis() + 100000);

        Set<Entry> allEntries = entryDao.getAll();

        assertEquals(2, allEntries.size());
    }

    @Test
    public void testCreateEntryWithNoneMilliseconds() {
        Entry created = entryDao.create("testUser");

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
        entryDao.closeConnection();

        assertNull(entryDao.create("testUser", System.currentTimeMillis() + 100000));
    }

    @Test
    public void testUpdateEntryWithSQLException() {
        Entry created = entryDao.create("testUser", System.currentTimeMillis() + 100000);
        entryDao.closeConnection();

        assertDoesNotThrow(() -> entryDao.update(created));
    }

    @Test
    public void testGetAllWithSQLException() {
        entryDao.closeConnection();

        assertTrue(entryDao.getAll().isEmpty());
    }
}
