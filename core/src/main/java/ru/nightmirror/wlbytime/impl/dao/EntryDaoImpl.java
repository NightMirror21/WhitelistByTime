package ru.nightmirror.wlbytime.impl.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.nightmirror.wlbytime.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntryDaoImpl implements EntryDao, AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(EntryDaoImpl.class.getSimpleName());

    private ConnectionSource connectionSource;
    private Dao<EntryTable, Long> entryDao;

    public EntryDaoImpl(DatabaseConfig config) {
        try {
            initConnection(config);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database connection", e);
            throw new DatabaseInitializationException("Failed to initialize database connection", e);
        }
    }

    private void initConnection(DatabaseConfig config) throws SQLException {
        if ("sqlite".equalsIgnoreCase(config.getType())) {
            String databaseUrl = config.getName().equals(":memory:") ?
                    "jdbc:sqlite::memory:" :
                    "jdbc:sqlite:" + config.getName() + ".db";
            connectionSource = new JdbcConnectionSource(databaseUrl);
        } else if ("mysql".equalsIgnoreCase(config.getType())) {
            String params = String.join("&", config.getParams());
            String databaseUrl = String.format("jdbc:mysql://%s/%s?%s", config.getAddress(), config.getName(), params);
            connectionSource = new JdbcConnectionSource(databaseUrl, config.getUser(), config.getPassword());
        } else {
            throw new UnsupportedDatabaseTypeException("Unsupported database type: " + config.getType());
        }

        entryDao = DaoManager.createDao(connectionSource, EntryTable.class);
        TableUtils.createTableIfNotExists(connectionSource, EntryTable.class);
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

    @Override
    public synchronized void close() {
        if (connectionSource != null) {
            try {
                connectionSource.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error closing database connection", e);
            }
        }
    }

    @Override
    public void update(Entry entry) {
        try {
            EntryTable entryTable = toEntryTable(entry);
            entryDao.createOrUpdate(entryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating entry", e);
            throw new DataAccessException("Failed to update entry", e);
        }
    }

    @Override
    public Optional<Entry> getLike(String nickname) {
        try {
            return entryDao.queryBuilder()
                    .where()
                    .like(EntryTable.NICKNAME_COLUMN, "%" + nickname + "%")
                    .query()
                    .stream()
                    .findFirst()
                    .map(this::fromEntryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying by like", e);
            throw new DataAccessException("Failed to query entries with like", e);
        }
    }

    @Override
    public Optional<Entry> get(String nickname) {
        try {
            return entryDao.queryBuilder()
                    .where()
                    .eq(EntryTable.NICKNAME_COLUMN, nickname)
                    .query()
                    .stream()
                    .findFirst()
                    .map(this::fromEntryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying by nickname", e);
            throw new DataAccessException("Failed to query entry by nickname", e);
        }
    }

    @Override
    public Entry create(String nickname, long milliseconds) {
        try {
            EntryTable entryTable = new EntryTable(null, nickname, milliseconds, null, null, null);
            entryDao.create(entryTable);
            return fromEntryTable(entryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating entry", e);
            throw new DataAccessException("Failed to create entry", e);
        }
    }

    @Override
    public Set<Entry> getAll() {
        try {
            Set<Entry> entries = new HashSet<>();
            for (EntryTable entryTable : entryDao.queryForAll()) {
                entries.add(fromEntryTable(entryTable));
            }
            return entries;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all entries", e);
            throw new DataAccessException("Failed to retrieve all entries", e);
        }
    }

    private EntryTable toEntryTable(Entry entry) {
        return new EntryTable(entry.getId(), entry.getNickname(), entry.getExpirationOrNull(),
                entry.getFrozenStartTimeOrNull(),
                entry.getFreezeEndTimeOrNull(),
                entry.getLastJoin());
    }

    private Entry fromEntryTable(EntryTable entryTable) {
        return Entry.builder()
                .id(entryTable.getId())
                .nickname(entryTable.getNickname())
                .until(entryTable.getUntil())
                .frozenAt(entryTable.getFrozenAt() != null ? new Timestamp(entryTable.getFrozenAt()) : null)
                .frozenUntil(entryTable.getFrozenUntil() != null ? new Timestamp(entryTable.getFrozenUntil()) : null)
                .lastJoin(entryTable.getLastJoin())
                .build();
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

    @DatabaseTable(tableName = "wlbytime_players")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    private static class EntryTable {
        public static final String ID_COLUMN = "id";
        public static final String NICKNAME_COLUMN = "nickname";
        public static final String UNTIL_COLUMN = "until";
        public static final String FROZEN_AT_COLUMN = "frozen_at";
        public static final String FROZEN_UNTIL_COLUMN = "frozen_until";
        public static final String LAST_JOIN_COLUMN = "last_join";

        @DatabaseField(generatedId = true, columnName = ID_COLUMN, canBeNull = false)
        private Long id;

        @DatabaseField(columnName = NICKNAME_COLUMN, canBeNull = false)
        private String nickname;

        @DatabaseField(columnName = UNTIL_COLUMN, canBeNull = false)
        private Long until;

        @DatabaseField(columnName = FROZEN_AT_COLUMN)
        private Long frozenAt;

        @DatabaseField(columnName = FROZEN_UNTIL_COLUMN)
        private Long frozenUntil;

        @DatabaseField(columnName = LAST_JOIN_COLUMN)
        private Timestamp lastJoin;
    }
}
