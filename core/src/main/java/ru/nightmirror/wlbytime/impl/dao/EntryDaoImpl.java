package ru.nightmirror.wlbytime.impl.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.entry.Expiration;
import ru.nightmirror.wlbytime.entry.Freezing;
import ru.nightmirror.wlbytime.entry.LastJoin;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntryDaoImpl implements EntryDao {

    private static final Logger LOGGER = Logger.getLogger(EntryDaoImpl.class.getSimpleName());
    private static final String SQLITE = "sqlite";
    private static final String MYSQL = "mysql";

    private ConnectionSource connectionSource;
    private TransactionManager transactionManager;
    private Dao<EntryTable, Long> entryDao;
    private Dao<LastJoinTable, Long> lastJoinDao;
    private Dao<FreezingTable, Long> freezingDao;
    private Dao<ExpirationTable, Long> expirationDao;
    private final @Nullable File dataFolder;

    public EntryDaoImpl(DatabaseConfig config) {
        try {
            dataFolder = null;
            com.j256.ormlite.logger.Logger.setGlobalLogLevel(com.j256.ormlite.logger.Level.OFF);
            initConnection(config);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database connection", e);
            throw new DatabaseInitializationException("Failed to initialize database connection", e);
        }
    }

    public EntryDaoImpl(@Nullable File dataFolder, DatabaseConfig config) {
        try {
            this.dataFolder = dataFolder;
            com.j256.ormlite.logger.Logger.setGlobalLogLevel(com.j256.ormlite.logger.Level.OFF);
            initConnection(config);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database connection", e);
            throw new DatabaseInitializationException("Failed to initialize database connection", e);
        }
    }

    private void initConnection(DatabaseConfig config) throws SQLException {
        String databaseUrl = getDatabaseUrl(config);
        connectionSource = SQLITE.equalsIgnoreCase(config.getType())
                ? new JdbcConnectionSource(databaseUrl)
                : new JdbcConnectionSource(databaseUrl, config.getUser(), config.getPassword());

        transactionManager = new TransactionManager(connectionSource);

        entryDao = DaoManager.createDao(connectionSource, EntryTable.class);
        lastJoinDao = DaoManager.createDao(connectionSource, LastJoinTable.class);
        freezingDao = DaoManager.createDao(connectionSource, FreezingTable.class);
        expirationDao = DaoManager.createDao(connectionSource, ExpirationTable.class);

        createTablesIfNotExist();
    }

    private String getDatabaseUrl(DatabaseConfig config) throws SQLException {
        if (SQLITE.equalsIgnoreCase(config.getType())) {
            String absolutePath;
            if (dataFolder != null) {
                absolutePath = new File(dataFolder, config.getName() + ".db").getAbsolutePath();
            } else {
                absolutePath = config.getName() + ".db";
            }
            return config.getName().equals(":memory:")
                    ? "jdbc:sqlite::memory:"
                    : "jdbc:sqlite:" + absolutePath;
        } else if (MYSQL.equalsIgnoreCase(config.getType())) {
            String params = String.join("&", config.getParams());
            return String.format("jdbc:mysql://%s/%s?%s", config.getAddress(), config.getName(), params);
        } else {
            throw new UnsupportedDatabaseTypeException("Unsupported database type: " + config.getType());
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        TableUtils.createTableIfNotExists(connectionSource, EntryTable.class);
        TableUtils.createTableIfNotExists(connectionSource, LastJoinTable.class);
        TableUtils.createTableIfNotExists(connectionSource, FreezingTable.class);
        TableUtils.createTableIfNotExists(connectionSource, ExpirationTable.class);
    }

    public synchronized void reopenConnection(DatabaseConfig newConfig) {
        try {
            close();
            initConnection(newConfig);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reopening database connection", e);
            throw new DatabaseInitializationException("Failed to reopen database connection", e);
        }
    }

    // FIXME move synchronized over connectionSource
    public synchronized void close() {
        try {
            connectionSource.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing database connection", e);
        }
    }

    @Override
    public void update(EntryImpl entry) {
        try {
            transactionManager.callInTransaction(() -> {
                EntryTable entryTable = new EntryTable(entry.getId(), entry.getNickname());
                entryDao.createOrUpdate(entryTable);
                updateExpirationTable(entry, entryTable);
                updateFreezingTable(entry, entryTable);
                updateLastJoinTable(entry, entryTable);
                return null;
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating entry entity", e);
            throw new DataAccessException("Failed to update entry entity", e);
        }
    }

    private void updateExpirationTable(EntryImpl entry, EntryTable entryTable) throws SQLException {
        if (entry.getExpiration() != null) {
            ExpirationTable existing = expirationDao.queryBuilder()
                    .where()
                    .eq(ExpirationTable.ENTRY_ID_COLUMN, entryTable.getId())
                    .queryForFirst();

            ExpirationTable newExpirationTable = getExpirationTable(entry, entryTable);

            if (existing != null) {
                newExpirationTable.setId(existing.getId());
            }

            expirationDao.createOrUpdate(newExpirationTable);
        } else {
            deleteByEntryId(expirationDao, ExpirationTable.ENTRY_ID_COLUMN, entry.getId());
        }
    }

    private void updateFreezingTable(EntryImpl entry, EntryTable entryTable) throws SQLException {
        if (entry.getFreezing() != null) {
            freezingDao.createOrUpdate(getFreezingTable(entry, entryTable));
        } else {
            deleteByEntryId(freezingDao, FreezingTable.ENTRY_ID_COLUMN, entry.getId());

        }
    }

    private void updateLastJoinTable(EntryImpl entry, EntryTable entryTable) throws SQLException {
        if (entry.getLastJoin() != null) {
            lastJoinDao.createOrUpdate(getLastJoinTable(entry, entryTable));
        } else {
            deleteByEntryId(lastJoinDao, LastJoinTable.ENTRY_ID_COLUMN, entry.getId());
        }
    }

    private static <T> void deleteByEntryId(Dao<T, Long> dao, String entryIdColumn, Long entryId) throws SQLException {
        DeleteBuilder<T, Long> builder = dao.deleteBuilder();
        builder.where().eq(entryIdColumn, entryId);
        dao.delete(builder.prepare());
    }

    private static @NotNull LastJoinTable getLastJoinTable(EntryImpl entry, EntryTable entryTable) {
        return new LastJoinTable(null, entryTable, Timestamp.from(entry.getLastJoin().getLastJoinTime()));
    }

    private static @NotNull FreezingTable getFreezingTable(EntryImpl entry, EntryTable entryTable) {
        return new FreezingTable(null, entryTable, Timestamp.from(entry.getFreezing().getStartTime()), Timestamp.from(entry.getFreezing().getEndTime()));
    }

    private static @NotNull ExpirationTable getExpirationTable(EntryImpl entry, EntryTable entryTable) {
        return new ExpirationTable(null, entryTable, Timestamp.from(entry.getExpiration().getExpirationTime()));
    }

    @Override
    public Optional<EntryImpl> get(String nickname) {
        try {
            EntryTable entryTable = entryDao.queryBuilder()
                    .where()
                    .eq(EntryTable.NICKNAME_COLUMN, nickname)
                    .queryForFirst();
            return getEntry(entryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying by nickname", e);
            throw new DataAccessException("Failed to query entry by nickname", e);
        }
    }

    @Override
    public Optional<EntryImpl> getLike(String nickname) {
        try {
            EntryTable entryTable = entryDao.queryBuilder()
                    .where()
                    .like(EntryTable.NICKNAME_COLUMN, nickname.toLowerCase())
                    .queryForFirst();
            return getEntry(entryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying by like", e);
            throw new DataAccessException("Failed to query entries with like", e);
        }
    }

    @NotNull
    private Optional<EntryImpl> getEntry(EntryTable entryTable) throws SQLException {
        if (entryTable == null) {
            return Optional.empty();
        }
        LastJoinTable lastJoinTable = lastJoinDao.queryBuilder()
                .where()
                .eq(LastJoinTable.ENTRY_ID_COLUMN, entryTable.getId())
                .queryForFirst();
        FreezingTable freezingTable = freezingDao.queryBuilder()
                .where()
                .eq(FreezingTable.ENTRY_ID_COLUMN, entryTable.getId())
                .queryForFirst();
        ExpirationTable expirationTable = expirationDao.queryBuilder()
                .where()
                .eq(ExpirationTable.ENTRY_ID_COLUMN, entryTable.getId())
                .queryForFirst();
        return Optional.of(fromEntryTables(entryTable, lastJoinTable, freezingTable, expirationTable));
    }

    @Override
    public EntryImpl create(String nickname, Instant until) {
        try {
            return transactionManager.callInTransaction(() -> {
                EntryTable entryTable = new EntryTable(null, nickname);
                entryDao.create(entryTable);
                ExpirationTable expirationTable = new ExpirationTable(null, entryTable, Timestamp.from(until));
                expirationDao.create(expirationTable);
                return get(nickname).orElseThrow();
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating entry", e);
            throw new DataAccessException("Failed to create entry", e);
        }
    }

    @Override
    public void remove(EntryImpl entry) {
        try {
            transactionManager.callInTransaction(() -> {
                DeleteBuilder<LastJoinTable, Long> lastJoinBuilder = lastJoinDao.deleteBuilder();
                lastJoinBuilder.where().eq(LastJoinTable.ENTRY_ID_COLUMN, entry.getId());
                lastJoinDao.delete(lastJoinBuilder.prepare());

                DeleteBuilder<FreezingTable, Long> freezingBuilder = freezingDao.deleteBuilder();
                freezingBuilder.where().eq(FreezingTable.ENTRY_ID_COLUMN, entry.getId());
                freezingDao.delete(freezingBuilder.prepare());

                DeleteBuilder<ExpirationTable, Long> expirationBuilder = expirationDao.deleteBuilder();
                expirationBuilder.where().eq(ExpirationTable.ENTRY_ID_COLUMN, entry.getId());
                expirationDao.delete(expirationBuilder.prepare());

                DeleteBuilder<EntryTable, Long> entryBuilder = entryDao.deleteBuilder();
                entryBuilder.where().eq(EntryTable.ID_COLUMN, entry.getId());
                entryDao.delete(entryBuilder.prepare());
                return null;
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing entry entity", e);
            throw new DataAccessException("Failed to remove entry entity", e);
        }
    }

    @Override
    public EntryImpl create(String nickname) {
        try {
            EntryTable entryTable = new EntryTable(null, nickname);
            entryDao.create(entryTable);
            return get(nickname).orElseThrow();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating entry", e);
            throw new DataAccessException("Failed to create entry", e);
        }
    }

    @Override
    public Set<EntryImpl> getAll() {
        try {
            Set<EntryImpl> entries = new HashSet<>();
            for (EntryTable entryTable : entryDao.queryForAll()) {
                LastJoinTable lastJoinTable = lastJoinDao.queryBuilder()
                        .where()
                        .eq(LastJoinTable.ENTRY_ID_COLUMN, entryTable.getId())
                        .queryForFirst();
                FreezingTable freezingTable = freezingDao.queryBuilder()
                        .where()
                        .eq(FreezingTable.ENTRY_ID_COLUMN, entryTable.getId())
                        .queryForFirst();
                ExpirationTable expirationTable = expirationDao.queryBuilder()
                        .where()
                        .eq(ExpirationTable.ENTRY_ID_COLUMN, entryTable.getId())
                        .queryForFirst();
                entries.add(fromEntryTables(entryTable, lastJoinTable, freezingTable, expirationTable));
            }
            return entries;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all entries", e);
            throw new DataAccessException("Failed to retrieve all entries", e);
        }
    }

    private EntryImpl fromEntryTables(EntryTable entryTable, LastJoinTable lastJoinTable,
                                      FreezingTable freezingTable, ExpirationTable expirationTable) {
        LastJoin lastJoin = lastJoinTable != null ? LastJoin.builder()
                .entryId(lastJoinTable.getId())
                .lastJoinTime(lastJoinTable.getLastJoin().toInstant())
                .build() : null;
        Freezing freezing = freezingTable != null ? Freezing.builder()
                .entryId(freezingTable.getId())
                .startTime(freezingTable.getStartTime().toInstant())
                .endTime(freezingTable.getEndTime().toInstant())
                .build() : null;
        Expiration expiration = expirationTable != null ? Expiration.builder()
                .entryId(expirationTable.getId())
                .expirationTime(expirationTable.getExpirationTime().toInstant())
                .build() : null;
        return EntryImpl.builder()
                .id(entryTable.getId())
                .nickname(entryTable.getNickname())
                .lastJoin(lastJoin)
                .freezing(freezing)
                .expiration(expiration)
                .build();
    }

    @DatabaseTable(tableName = "wlbytime_entries")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class EntryTable {
        public static final String ID_COLUMN = "id";
        public static final String NICKNAME_COLUMN = "nickname";

        @DatabaseField(generatedId = true, columnName = ID_COLUMN, canBeNull = false)
        private Long id;

        @DatabaseField(columnName = NICKNAME_COLUMN, canBeNull = false, unique = true)
        private String nickname;
    }

    @DatabaseTable(tableName = "wlbytime_lastjoins")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class LastJoinTable {
        public static final String ENTRY_ID_COLUMN = "entry_id";
        public static final String LAST_JOIN_COLUMN = "last_join";

        @DatabaseField(generatedId = true, columnName = "id", canBeNull = false)
        private Long id;

        @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = ENTRY_ID_COLUMN, canBeNull = false, unique = true)
        private EntryTable entry;

        @DatabaseField(columnName = LAST_JOIN_COLUMN, canBeNull = false)
        private Timestamp lastJoin;
    }

    @DatabaseTable(tableName = "wlbytime_freezings")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class FreezingTable {
        public static final String ENTRY_ID_COLUMN = "entry_id";
        public static final String START_TIME_COLUMN = "start_time";
        public static final String END_TIME_COLUMN = "end_time";

        @DatabaseField(generatedId = true, columnName = "id", canBeNull = false)
        private Long id;

        @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = ENTRY_ID_COLUMN, canBeNull = false, unique = true)
        private EntryTable entry;

        @DatabaseField(columnName = START_TIME_COLUMN, canBeNull = false)
        private Timestamp startTime;

        @DatabaseField(columnName = END_TIME_COLUMN, canBeNull = false)
        private Timestamp endTime;
    }

    @DatabaseTable(tableName = "wlbytime_expirations")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ExpirationTable {
        public static final String ENTRY_ID_COLUMN = "entry_id";
        public static final String EXPIRATION_TIME_COLUMN = "expiration_time";

        @DatabaseField(generatedId = true, columnName = "id", canBeNull = false)
        private Long id;

        @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = ENTRY_ID_COLUMN, canBeNull = false, unique = true)
        private EntryTable entry;

        @DatabaseField(columnName = EXPIRATION_TIME_COLUMN, canBeNull = false)
        private Timestamp expirationTime;
    }

    public static class DataAccessException extends RuntimeException {
        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class DatabaseInitializationException extends RuntimeException {
        public DatabaseInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UnsupportedDatabaseTypeException extends RuntimeException {
        public UnsupportedDatabaseTypeException(String message) {
            super(message);
        }
    }
}