package ru.nightmirror.wlbytime.impl.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntryDaoImpl implements EntryDao {

    private static final Logger LOGGER = Logger.getLogger(EntryDaoImpl.class.getName());

    private ConnectionSource connectionSource;
    private Dao<EntryTable, Long> entryDao;

    public EntryDaoImpl(DatabaseConfig config) {
        try {
            initConnection(config);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database connection", e);
        }
    }

    private void initConnection(DatabaseConfig config) throws SQLException {
        if (config.getType().equalsIgnoreCase("sqlite")) {
            String databaseUrl;
            if (config.getName().equals(":memory:")) {
                databaseUrl = "jdbc:sqlite::memory:";
            } else {
                databaseUrl = "jdbc:sqlite:" + config.getName() + ".db";
            }
            connectionSource = new JdbcConnectionSource(databaseUrl);
        } else {
            String databaseUrl = "jdbc:mysql://" + config.getAddress() + "/" + config.getName()
                    + "?" + String.join("&", config.getParams());
            connectionSource = new JdbcConnectionSource(databaseUrl, config.getUser(), config.getPassword());
        }

        entryDao = DaoManager.createDao(connectionSource, EntryTable.class);
        TableUtils.createTableIfNotExists(connectionSource, EntryTable.class);
    }

    public void reopenConnection(DatabaseConfig newConfig) {
        try {
            closeConnection();
            initConnection(newConfig);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reopening database connection", e);
        }
    }

    public void closeConnection() {
        try {
            if (connectionSource != null) {
                connectionSource.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing database connection", e);
        }
    }

    @Override
    public void update(Entry entry) {
        try {
            EntryTable entryTable = toEntryTable(entry);
            entryDao.createOrUpdate(entryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating entry", e);
        }
    }

    @Override
    public Optional<Entry> getLike(String nickname) {
        try {
            return entryDao.queryBuilder().where()
                    .like(EntryTable.NICKNAME_COLUMN, nickname).query()
                    .stream().findFirst().map(this::fromEntryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying by like", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Entry> get(String nickname) {
        try {
            return entryDao.queryBuilder().where()
                    .eq(EntryTable.NICKNAME_COLUMN, nickname).query()
                    .stream().findFirst().map(this::fromEntryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying by nickname", e);
            return Optional.empty();
        }
    }

    @Override
    public Entry create(String nickname, long milliseconds) {
        try {
            EntryTable entryTable = new EntryTable(null, nickname, milliseconds, null, null);
            entryDao.create(entryTable);
            return fromEntryTable(entryTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating entry", e);
            return null;
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
            return Set.of();
        }
    }

    private EntryTable toEntryTable(Entry entry) {
        return new EntryTable(entry.getId(), entry.getNickname(), entry.getUntilOrNull(),
                entry.getFrozenAtOrNull(),
                entry.getFrozenUntilOrNull());
    }

    private Entry fromEntryTable(EntryTable entryTable) {
        return Entry.builder()
                .id(entryTable.getId())
                .nickname(entryTable.getNickname())
                .until(entryTable.getUntil())
                .frozenAt(entryTable.getFrozenAt())
                .frozenUntil(entryTable.getFrozenUntil())
                .build();
    }

    @DatabaseTable(tableName = "wlbytime_players")
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    @ToString
    private static final class EntryTable {
        public static final String ID_COLUMN = "id";
        public static final String NICKNAME_COLUMN = "nickname";
        public static final String UNTIL_COLUMN = "until";
        public static final String FROZEN_AT_COLUMN = "frozen_at";
        public static final String FROZEN_UNTIL_COLUMN = "frozen_until";

        @DatabaseField(generatedId = true, columnName = ID_COLUMN, canBeNull = false)
        Long id;

        @DatabaseField(columnName = NICKNAME_COLUMN, canBeNull = false)
        String nickname;

        @DatabaseField(columnName = UNTIL_COLUMN, canBeNull = false)
        Long until;

        @DatabaseField(columnName = FROZEN_AT_COLUMN)
        Long frozenAt;

        @DatabaseField(columnName = FROZEN_UNTIL_COLUMN)
        Long frozenUntil;
    }
}
