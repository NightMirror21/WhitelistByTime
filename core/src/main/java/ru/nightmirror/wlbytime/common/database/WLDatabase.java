package ru.nightmirror.wlbytime.common.database;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
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
import ru.nightmirror.wlbytime.common.database.misc.WLPlayer;
import ru.nightmirror.wlbytime.common.database.misc.WLPlayerMapper;
import ru.nightmirror.wlbytime.common.database.misc.WLPlayerTable;
import ru.nightmirror.wlbytime.interfaces.listener.PlayerListener;
import ru.nightmirror.wlbytime.interfaces.listener.PlayerListenersContainer;
import ru.nightmirror.wlbytime.interfaces.database.CachedDatabase;
import ru.nightmirror.wlbytime.interfaces.database.Mapper;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class WLDatabase implements PlayerAccessor, CachedDatabase, PlayerListenersContainer {

    @Setter
    DatabaseSettings settings;
    @Getter
    boolean connected = false;

    final Mapper<WLPlayerTable, WLPlayer> mapper;

    JdbcPooledConnectionSource connection;
    LoadingCache<String, WLPlayer> cache;
    final List<PlayerListener> listeners = new ArrayList<>();

    public WLDatabase(DatabaseSettings settings) {
        this.settings = settings;
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.OFF);
        mapper = new WLPlayerMapper();
        if (!createConnection()) return;
        cache = Caffeine.newBuilder()
                .refreshAfterWrite(Duration.ofMinutes(5))
                .build(key -> getPlayer(key).join().orElse(null));
    }

    private boolean createConnection() {
        connected = false;
        try {
            connection = getConnectionSource();
            TableUtils.createTableIfNotExists(connection, WLPlayerTable.class);
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

    private CompletableFuture<Dao<WLPlayerTable, Long>> getDao() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return DaoManager.createDao(connection, WLPlayerTable.class);
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
        return getPlayers().thenAccept(list -> list.forEach(player -> cache.put(player.getNickname(), player)));
    }

    @Override
    public CompletableFuture<Optional<WLPlayer>> getPlayer(@NotNull String nickname) {
        return getDao().thenApply((dao) -> {
            try {
                Optional<WLPlayerTable> tableOptional = dao.queryForEq(WLPlayerTable.NICKNAME_COLUMN, nickname).stream().findAny();
                if (tableOptional.isEmpty()) return Optional.empty();
                return Optional.of(mapper.toEntity(tableOptional.get()));
            } catch (SQLException exception) {
                exception.printStackTrace();
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<WLPlayer> getPlayerCached(@NotNull String nickname) {
        return Optional.ofNullable(cache.getIfPresent(nickname));
    }

    @Override
    public void loadPlayerToCache(@NotNull String nickname) {
        cache.refresh(nickname);
    }

    @Override
    public void loadPlayersToCache(@NotNull List<String> nicknames) {
        cache.refreshAll(nicknames);
    }

    @Override
    public CompletableFuture<Boolean> createOrUpdate(@NotNull WLPlayer player) {
        return getDao().thenApply((dao) -> {
            try {
                Dao.CreateOrUpdateStatus status = dao.createOrUpdate(mapper.toTable(player));
                if (status.isCreated() || status.isUpdated()) {
                    cache.refresh(player.getNickname());
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
    public CompletableFuture<Boolean> delete(@NotNull WLPlayer player) {
        return getDao().thenApply((dao) -> {
            try {
                if (dao.delete(mapper.toTable(player)) == 1) {
                    cache.invalidate(player.getNickname());
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
        return getDao().thenApply((dao) -> {
            try {
                WLPlayer player = getPlayer(nickname).join().orElse(null);
                if (player == null) return false;
                if (dao.delete(mapper.toTable(player)) == 1) {
                    cache.invalidate(player.getNickname());
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
    public CompletableFuture<Void> delete(@NotNull List<WLPlayer> players) {
        return getDao().thenAccept((dao) -> {
            try {
                if (dao.delete(players.stream().map(mapper::toTable).toList()) == 1) {
                    cache.invalidateAll(players.stream().map(WLPlayer::getNickname).toList());
                    players.forEach(player -> listeners.forEach(listener -> listener.playerRemoved(player)));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<List<WLPlayer>> getPlayers() {
        return getDao().thenApply((dao) -> {
            try {
                return dao.queryForAll().stream().map(t -> mapper.toEntity(t)).toList();
            } catch (Exception exception) {
                exception.printStackTrace();
                return List.of();
            }
        });
    }

    @Override
    public List<WLPlayer> getPlayersCached() {
        return cache.asMap().values().stream().toList();
    }

    @Override
    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }
}
