package ru.nightmirror.wlbytime.common.database;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.table.TableUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.common.database.misc.DatabaseSettings;
import ru.nightmirror.wlbytime.common.database.misc.PlayerData;
import ru.nightmirror.wlbytime.common.database.misc.PlayerDataMapper;
import ru.nightmirror.wlbytime.common.database.misc.PlayerDataTable;
import ru.nightmirror.wlbytime.interfaces.database.Database;
import ru.nightmirror.wlbytime.interfaces.database.Mapper;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;
import ru.nightmirror.wlbytime.interfaces.listener.PlayerListener;
import ru.nightmirror.wlbytime.interfaces.listener.PlayerListenersContainer;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class DatabaseImpl implements PlayerAccessor, Database, PlayerListenersContainer {

    @Setter
    DatabaseSettings settings;
    @Getter
    boolean connected = false;

    final Mapper<PlayerDataTable, PlayerData> mapper;

    JdbcPooledConnectionSource connection;

    final AsyncLoadingCache<String, PlayerData> cache;

    final List<PlayerListener> listeners = new ArrayList<>();

    public DatabaseImpl(DatabaseSettings settings) throws SQLException {
        this.settings = settings;
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.OFF);
        mapper = new PlayerDataMapper();
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
    public CompletableFuture<Boolean> reconnect() {
        return CompletableFuture.supplyAsync(this::createConnection);
    }

    @Override
    public CompletableFuture<Boolean> close() {
        return CompletableFuture.supplyAsync(() -> {
            if (isConnected()) {
                try {
                    connection.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    return false;
                }
            }
            return true;
        });
    }

    @Override
    public CompletableFuture<Void> refreshCache() {
        return getPlayers().thenAccept(list -> list.forEach(player -> cache.synchronous().put(player.getNickname(), player)));
    }

    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayer(@NotNull String nickname) {
        return getDao().thenApply((dao) -> {
            try {
                return dao.queryForEq(PlayerDataTable.NICKNAME_COLUMN, nickname)
                        .stream()
                        .findAny()
                        .map(mapper::toEntity);
            } catch (SQLException exception) {
                exception.printStackTrace();
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<PlayerData> getPlayerCached(@NotNull String nickname) {
        return Optional.ofNullable(cache.synchronous().getIfPresent(nickname));
    }

    @Override
    public void loadPlayerToCache(@NotNull String nickname) {
        cache.synchronous().refresh(nickname);
    }

    @Override
    public void loadPlayersToCache(@NotNull List<String> nicknames) {
        cache.synchronous().refreshAll(nicknames);
    }

    @Override
    public CompletableFuture<Boolean> createOrUpdate(@NotNull PlayerData player) {
        return getDao().thenApply((dao) -> {
            try {
                Dao.CreateOrUpdateStatus status = dao.createOrUpdate(mapper.toTable(player));
                if (status.isCreated() || status.isUpdated()) {
                    cache.synchronous().refresh(player.getNickname());
                    return true;
                }
                return false;
            } catch (Exception exception) {
                exception.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(@NotNull PlayerData player) {
        return getDao().thenApply((dao) -> {
            try {
                if (dao.delete(mapper.toTable(player)) == 1) {
                    cache.synchronous().invalidate(player.getNickname());
                    listeners.forEach(listener -> listener.playerRemoved(player));
                    return true;
                }
                return false;
            } catch (Exception exception) {
                exception.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(@NotNull String nickname) {

        return getDao().thenCombine(getPlayer(nickname), (dao, playerOptional) -> {
            try {
                PlayerData player = playerOptional.orElse(null);
                if (player != null && dao.delete(mapper.toTable(player)) == 1) {
                    cache.synchronous().invalidate(player.getNickname());
                    listeners.forEach(listener -> listener.playerRemoved(player));
                    return true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        });
    }

    @Override
    public CompletableFuture<Void> delete(@NotNull List<PlayerData> players) {
        return getDao().thenAccept((dao) -> {
            try {
                if (dao.delete(players.stream().map(mapper::toTable).toList()) == 1) {
                    cache.synchronous().invalidateAll(players.stream().map(PlayerData::getNickname).toList());
                    players.forEach(player -> listeners.forEach(listener -> listener.playerRemoved(player)));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<List<PlayerData>> getPlayers() {
        return getDao().thenApply((dao) -> {
            try {
                return dao.queryForAll().stream().map(mapper::toEntity).toList();
            } catch (Exception exception) {
                exception.printStackTrace();
                return List.of();
            }
        });
    }

    @Override
    public List<PlayerData> getPlayersCached() {
        return cache.synchronous().asMap().values().stream().toList();
    }

    @Override
    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }
}
