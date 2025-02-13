package ru.nightmirror.wlbytime.impl.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.entry.Freezing;

import java.io.File;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class EntryDaoImplTest {

    private EntryDaoImpl entryDao;
    private DatabaseConfig databaseConfig;

    @BeforeEach
    public void setUp() {
        databaseConfig = new DatabaseConfig();
        databaseConfig.setType("sqlite");
        databaseConfig.setName(":memory:");
        entryDao = new EntryDaoImpl(databaseConfig);
    }

    @AfterEach
    public void tearDown() {
        entryDao.close();
    }

    @Test
    public void initializationWithValidConfigCreatesDao() {
        assertNotNull(entryDao);
    }

    @Test
    public void initializationWithInvalidDatabaseTypeThrowsException() {
        databaseConfig.setType("unsupported_db_type");
        assertThrows(EntryDaoImpl.UnsupportedDatabaseTypeException.class, () -> new EntryDaoImpl(databaseConfig));
    }

    @Test
    public void constructorWithDataFolderCreatesDao() {
        File folder = new File(System.getProperty("java.io.tmpdir"), "testDaoFolder");
        folder.mkdirs();
        EntryDaoImpl dao = new EntryDaoImpl(folder, databaseConfig);
        assertNotNull(dao);
    }

    @Test
    public void reopenConnectionWithNewConfigReinitializesDao() {
        DatabaseConfig newConfig = new DatabaseConfig();
        newConfig.setType("sqlite");
        newConfig.setName(":memory:");
        assertDoesNotThrow(() -> entryDao.reopenConnection(newConfig));
    }

    @Test
    public void reopenConnectionThrowsOnFailure() {
        DatabaseConfig config = new DatabaseConfig();
        config.setType("unsupported");
        assertThrows(EntryDaoImpl.UnsupportedDatabaseTypeException.class, () -> entryDao.reopenConnection(config));
    }

    @Test
    public void createEntriesWithEmptySet() {
        Set<String> emptySet = Collections.emptySet();
        entryDao.create(emptySet);
        Set<EntryImpl> allEntries = entryDao.getAll();
        assertTrue(allEntries.isEmpty());
    }

    @Test
    public void createEntriesWithMultipleNicknames() {
        Set<String> nicknames = Set.of("user1", "user2", "user3");
        entryDao.create(nicknames);

        Set<EntryImpl> allEntries = entryDao.getAll();
        assertEquals(3, allEntries.size());

        Set<String> retrievedNicknames = allEntries.stream()
                .map(EntryImpl::getNickname)
                .collect(Collectors.toSet());
        assertTrue(retrievedNicknames.containsAll(nicknames));
    }


    @Test
    public void closeMultipleTimesDoesNotThrowError() {
        assertDoesNotThrow(() -> entryDao.close());
        assertDoesNotThrow(() -> entryDao.close());
    }

    @Test
    public void updateEntryWithFreezingPersistsFreezing() {
        EntryImpl entry = entryDao.create("freezing_test");
        Freezing freezing = new Freezing(entry.getId(), Duration.ofSeconds(10));
        entry.setFreezing(freezing);
        entryDao.update(entry);
        Optional<EntryImpl> retrieved = entryDao.get("freezing_test");
        assertTrue(retrieved.isPresent());
        assertNotNull(retrieved.get().getFreezing());
        assertEquals(Duration.ofSeconds(10), retrieved.get().getFreezing().getDurationOfFreeze());
    }

    @Test
    public void updateEntryWithLastJoinPersistsLastJoin() {
        EntryImpl entry = entryDao.create("lastjoin_test");
        entry.updateLastJoin();
        entryDao.update(entry);
        Optional<EntryImpl> retrieved = entryDao.get("lastjoin_test");
        assertTrue(retrieved.isPresent());
        assertNotNull(retrieved.get().getLastJoin());
        assertTrue(retrieved.get().isJoined());
    }

    @Test
    public void createEntryDuplicateNicknameThrowsException() {
        entryDao.create("duplicate_test");
        assertThrows(EntryDaoImpl.DataAccessException.class, () -> entryDao.create("duplicate_test"));
    }

    @Test
    public void getEntryWithAllRelatedDataReturnsCompleteEntry() {
        String nickname = "full_entry_test";
        EntryImpl entry = entryDao.create(nickname, Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        entry.updateLastJoin();
        entryDao.update(entry);
        Optional<EntryImpl> retrieved = entryDao.get(nickname);
        assertTrue(retrieved.isPresent());
        EntryImpl fullEntry = retrieved.get();
        assertNotNull(fullEntry.getExpiration());
        assertNotNull(fullEntry.getFreezing());
        assertNotNull(fullEntry.getLastJoin());
    }

    @Test
    public void createEntryWithNicknameCreatesEntryInDatabase() {
        String nickname = "test_nickname";
        EntryImpl entry = entryDao.create(nickname);
        assertNotNull(entry);
        assertEquals(nickname, entry.getNickname());
        Optional<EntryImpl> retrieved = entryDao.get(nickname);
        assertTrue(retrieved.isPresent());
        assertEquals(nickname, retrieved.get().getNickname());
    }

    @Test
    public void createEntryWithExpirationTimeCreatesEntryWithExpiration() {
        String nickname = "expiring_nickname";
        Instant expectedTime = Instant.now().plus(Duration.ofSeconds(10));
        EntryImpl entry = entryDao.create(nickname, expectedTime);
        assertNotNull(entry);
        assertEquals(nickname, entry.getNickname());
        assertNotNull(entry.getExpiration());
        Instant actualTime = entry.getExpiration().getExpirationTime();
        long tolerance = 100;
        assertTrue(Math.abs(actualTime.toEpochMilli() - expectedTime.toEpochMilli()) <= tolerance);
    }

    @Test
    public void getEntryByExistingNicknameReturnsEntry() {
        String nickname = "existing_nickname";
        entryDao.create(nickname);
        Optional<EntryImpl> result = entryDao.get(nickname);
        assertTrue(result.isPresent());
        assertEquals(nickname, result.get().getNickname());
    }

    @Test
    public void getEntryByNonExistingNicknameReturnsEmptyOptional() {
        Optional<EntryImpl> result = entryDao.get("non_existing_nickname");
        assertFalse(result.isPresent());
    }

    @Test
    public void getEntryLikeBySimilarNicknameReturnsMatchingEntry() {
        String nickname = "SIMILAR";
        entryDao.create(nickname);
        Optional<EntryImpl> result = entryDao.getLike("similar");
        assertTrue(result.isPresent());
        assertEquals(nickname, result.get().getNickname());
    }

    @Test
    public void getEntryLikeByNoMatchingNicknameReturnsEmptyOptional() {
        entryDao.create("some_nickname");
        Optional<EntryImpl> result = entryDao.getLike("unmatched");
        assertFalse(result.isPresent());
    }

    @Test
    public void getAllReturnsAllEntries() {
        entryDao.create("first_nickname");
        entryDao.create("second_nickname");
        Set<EntryImpl> all = entryDao.getAll();
        assertEquals(2, all.size());
    }

    @Test
    public void updateEntryModifiesExistingEntry() {
        EntryImpl entry = entryDao.create("updatable_nickname");
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entryDao.update(entry);
        Optional<EntryImpl> result = entryDao.get(entry.getNickname());
        assertTrue(result.isPresent());
        assertNotNull(result.get().getExpiration());
    }

    @Test
    public void updateEntryRemoveExpirationDeletesExpirationRecord() {
        EntryImpl entry = entryDao.create("entry_with_expiration");
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entryDao.update(entry);
        entry.setForever();
        entryDao.update(entry);
        Optional<EntryImpl> result = entryDao.get(entry.getNickname());
        assertTrue(result.isPresent());
        assertNull(result.get().getExpiration());
    }

    @Test
    public void updateEntryRemoveFreezingDeletesFreezingRecord() {
        String nickname = "freezing_removal";
        EntryImpl entry = entryDao.create(nickname, Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        entryDao.update(entry);
        Optional<EntryImpl> before = entryDao.get(nickname);
        assertTrue(before.isPresent());
        assertNotNull(before.get().getFreezing());
        entry.setFreezing(null);
        entryDao.update(entry);
        Optional<EntryImpl> after = entryDao.get(nickname);
        assertTrue(after.isPresent());
        assertNull(after.get().getFreezing());
    }

    @Test
    public void updateEntryRemoveLastJoinDeletesLastJoinRecord() {
        String nickname = "lastjoin_removal";
        EntryImpl entry = entryDao.create(nickname);
        entry.updateLastJoin();
        entryDao.update(entry);
        Optional<EntryImpl> before = entryDao.get(nickname);
        assertTrue(before.isPresent());
        assertNotNull(before.get().getLastJoin());
        entry.setLastJoin(null);
        entryDao.update(entry);
        Optional<EntryImpl> after = entryDao.get(nickname);
        assertTrue(after.isPresent());
        assertNull(after.get().getLastJoin());
    }

    @Test
    public void removeEntryDeletesEntryFromDatabase() {
        String nickname = "removable_nickname";
        EntryImpl entry = entryDao.create(nickname);
        entryDao.remove(entry);
        Optional<EntryImpl> result = entryDao.get(nickname);
        assertFalse(result.isPresent());
    }

    @Test
    public void removeEntryDeletesRelatedTables() {
        String nickname = "removable_with_related_data";
        EntryImpl entry = entryDao.create(nickname, Instant.now().plus(Duration.ofSeconds(10)));
        entryDao.remove(entry);
        Optional<EntryImpl> result = entryDao.get(nickname);
        assertFalse(result.isPresent());
        Set<EntryImpl> all = entryDao.getAll();
        assertEquals(0, all.size());
    }

    @Test
    public void removeEntryWhenEntryDoesNotExistDoesNothing() {
        EntryImpl nonExisting = EntryImpl.builder().id(999L).nickname("non_existing").build();
        assertDoesNotThrow(() -> entryDao.remove(nonExisting));
    }

    @Test
    public void updateEntryTransactionRollbackOnFailure() {
        EntryImpl entry = entryDao.create("rollback_test");
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.setNickname(null);
        assertThrows(EntryDaoImpl.DataAccessException.class, () -> entryDao.update(entry));
        Optional<EntryImpl> retrieved = entryDao.get("rollback_test");
        assertTrue(retrieved.isPresent());
        assertEquals("rollback_test", retrieved.get().getNickname());
    }

    @Test
    public void getDatabaseUrlSqliteMemoryBranch() throws Exception {
        DatabaseConfig config = new DatabaseConfig();
        config.setType("sqlite");
        config.setName(":memory:");
        Method method = EntryDaoImpl.class.getDeclaredMethod("getDatabaseUrl", DatabaseConfig.class);
        method.setAccessible(true);
        String url = (String) method.invoke(entryDao, config);
        assertEquals("jdbc:sqlite::memory:", url);
    }

    @Test
    public void getDatabaseUrlSqliteFileNoDataFolderBranch() throws Exception {
        DatabaseConfig config = new DatabaseConfig();
        config.setType("sqlite");
        config.setName("testdb");
        Method method = EntryDaoImpl.class.getDeclaredMethod("getDatabaseUrl", DatabaseConfig.class);
        method.setAccessible(true);
        String url = (String) method.invoke(entryDao, config);
        assertTrue(url.startsWith("jdbc:sqlite:"));
        assertTrue(url.contains("testdb.db"));
    }

    @Test
    public void getDatabaseUrlSqliteWithDataFolderBranch() throws Exception {
        DatabaseConfig config = new DatabaseConfig();
        config.setType("sqlite");
        config.setName("testdb");
        File folder = new File(System.getProperty("java.io.tmpdir"), "folderTest");
        folder.mkdirs();
        EntryDaoImpl daoWithFolder = new EntryDaoImpl(folder, config);
        Method method = EntryDaoImpl.class.getDeclaredMethod("getDatabaseUrl", DatabaseConfig.class);
        method.setAccessible(true);
        String url = (String) method.invoke(daoWithFolder, config);
        String expectedPath = new File(folder, "testdb.db").getAbsolutePath();
        assertTrue(url.startsWith("jdbc:sqlite:"));
        assertTrue(url.contains(expectedPath));
    }

    @Test
    public void getDatabaseUrlMysqlBranch() throws Exception {
        DatabaseConfig config = new DatabaseConfig();
        config.setType("mysql");
        config.setAddress("localhost");
        config.setName("testdb");
        config.setParams(List.of("autoReconnect=true", "useSSL=false"));
        config.setUser("user");
        config.setPassword("pass");
        Method method = EntryDaoImpl.class.getDeclaredMethod("getDatabaseUrl", DatabaseConfig.class);
        method.setAccessible(true);
        String url = (String) method.invoke(entryDao, config);
        assertEquals("jdbc:mysql://localhost/testdb?autoReconnect=true&useSSL=false", url);
    }

    @Test
    public void getDatabaseUrlUnsupportedBranchThrowsException() throws Exception {
        DatabaseConfig config = new DatabaseConfig();
        config.setType("unsupported");
        Method method = EntryDaoImpl.class.getDeclaredMethod("getDatabaseUrl", DatabaseConfig.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(entryDao, config));
    }
}
