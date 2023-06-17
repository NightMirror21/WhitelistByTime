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
import ru.nightmirror.wlbytime.interfaces.database.CachedDatabase;
import ru.nightmirror.wlbytime.interfaces.database.Mapper;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class WLDatabase implements PlayerAccessor, CachedDatabase {

    @Setter
    DatabaseSettings settings;
    @Getter
    boolean connected = false;

    Mapper<WLPlayerTable, WLPlayer> mapper;

    Dao<WLPlayerTable, Long> dao;
    JdbcPooledConnectionSource connection;

    LoadingCache<String, WLPlayer> cache;

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
            dao = DaoManager.createDao(connection, WLPlayerTable.class);
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
        return CompletableFuture.runAsync(() -> getPlayers().join().forEach(player -> cache.put(player.getNickname(), player)));
    }

    @Override
    public CompletableFuture<Optional<WLPlayer>> getPlayer(@NotNull String nickname) {
        return CompletableFuture.supplyAsync(() -> {
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
    public CompletableFuture<Boolean> createOrUpdate(@NotNull WLPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                dao.createOrUpdate(mapper.toTable(player));
                cache.refresh(player.getNickname());
                return true;
            } catch (Exception exception) {
                exception.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(@NotNull String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                WLPlayer player = getPlayer(nickname).join().orElse(null);
                if (player == null) return false;
                dao.delete(mapper.toTable(player));
                cache.invalidate(player.getNickname());
                return true;
            } catch (Exception exception) {
                exception.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<List<WLPlayer>> getPlayers() {
        return CompletableFuture.supplyAsync(() -> {
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
}
