package ru.nightmirror.wlbytime.impl.database;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.table.TableUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.interfaces.database.PlayerDao;
import ru.nightmirror.wlbytime.misc.Mapper;
import ru.nightmirror.wlbytime.models.DatabaseSettings;
import ru.nightmirror.wlbytime.models.PlayerData;
import ru.nightmirror.wlbytime.models.PlayerDataMapperImpl;
import ru.nightmirror.wlbytime.models.PlayerDataTable;

import java.io.Closeable;
import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerDaoImpl implements PlayerDao, Closeable {

    final DatabaseSettings settings;
    final Boolean caseSensitive;
    final Mapper<PlayerDataTable, PlayerData> mapper;
    final AsyncLoadingCache<String, PlayerData> cache;
    @Getter
    boolean connected = false;
    JdbcPooledConnectionSource connection;

    public PlayerDaoImpl(DatabaseSettings settings, Boolean caseSensitive) throws SQLException {
        this.settings = settings;
        this.caseSensitive = caseSensitive;
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.OFF);
        mapper = new PlayerDataMapperImpl();
        if (!createConnection()) throw new SQLException("Can't create connection");
        cache = Caffeine.newBuilder()
                .refreshAfterWrite(Duration.ofMinutes(5))
                .buildAsync((key, executor) -> getPlayer(key).thenApply(v -> v.orElse(null)));
    }

    private boolean createConnection() {
        connected = false;
        try {
            connection = getConnectionSource();
            TableUtils.createTableIfNotExists(connection, PlayerDataTable.class);
            connected = true;
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    private JdbcPooledConnectionSource getConnectionSource() throws SQLException {
        if (settings.getType().equalsIgnoreCase("sqlite") || settings.getType().equalsIgnoreCase("h2"))
            return new JdbcPooledConnectionSource("jdbc:" + settings.getType() + ":" + new File(settings.getLocalStorageDir(), "database.db").getAbsolutePath());

        return new JdbcPooledConnectionSource("jdbc:" + settings.getType() + "://" + settings.getAddress() + File.separator + settings.getDatabaseName()
                + (!settings.getParams().isEmpty() ? "?" + String.join("&", settings.getParams()) : ""),
                settings.getUser(),
                settings.getPassword()
        );
    }

    private CompletableFuture<Dao<PlayerDataTable, Long>> getDao() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return DaoManager.createDao(connection, PlayerDataTable.class);
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
        });
    }

    @Override
    public void close() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayer(@NotNull String nickname) {
        return getDao().thenApply((dao) -> {
            try {
                if (caseSensitive) {
                    return dao.queryForEq(PlayerDataTable.NICKNAME_COLUMN, nickname)
                            .stream()
                            .findAny()
                            .map(mapper::toEntity);
                } else {
                    return dao.query(dao.queryBuilder()
                                    .where()
                                    .like(PlayerDataTable.NICKNAME_COLUMN, nickname)
                                    .prepare())
                            .stream()
                            .findAny()
                            .map(mapper::toEntity);
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<PlayerData> getPlayerCached(@NotNull String nickname) {
        if (caseSensitive) {
            return Optional.ofNullable(cache.synchronous().getIfPresent(nickname));
        } else {
            return cache.synchronous()
                    .asMap()
                    .values()
                    .stream()
                    .filter(data -> data.getNickname().equalsIgnoreCase(nickname))
                    .findAny();
        }
    }

    @Override
    public void loadPlayerToCache(@NotNull String nickname) {
        if (caseSensitive) {
            cache.synchronous().refresh(nickname);
        } else {
            cache.synchronous()
                    .asMap()
                    .values()
                    .stream()
                    .map(PlayerData::getNickname)
                    .filter(dataNickname -> dataNickname.equalsIgnoreCase(nickname))
                    .forEach(dataNickname -> cache.synchronous().refresh(dataNickname));
        }
    }

    @Override
    public void loadPlayersToCache(@NotNull Set<String> nicknames) {
        nicknames.forEach(this::loadPlayerToCache);
    }

    @Override
    public CompletableFuture<Boolean> createOrUpdate(@NotNull PlayerData player) {
        return getDao().thenApply((dao) -> {
            try {
                Dao.CreateOrUpdateStatus status = dao.createOrUpdate(mapper.toTable(player));
                if (status.isCreated() || status.isUpdated()) {
                    loadPlayerToCache(player.getNickname());
                    return true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        });
    }

    @Override
    public CompletableFuture<Set<PlayerData>> getPlayers() {
        return getDao().thenApply((dao) -> {
            try {
                return dao.queryForAll().stream().map(mapper::toEntity).collect(Collectors.toSet());
            } catch (Exception exception) {
                exception.printStackTrace();
                return Set.of();
            }
        });
    }

    @Override
    public Set<PlayerData> getPlayersCached() {
        return new HashSet<>(cache.synchronous().asMap().values());
    }
}
