package ru.nightmirror.wlbytime.interfaces.database;

import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.common.database.misc.PlayerData;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PlayerDao {
    CompletableFuture<Optional<PlayerData>> getPlayer(@NotNull String nickname);

    Optional<PlayerData> getPlayerCached(@NotNull String nickname);

    void loadPlayerToCache(@NotNull String nickname);

    void loadPlayersToCache(@NotNull List<String> nicknames);

    CompletableFuture<Boolean> createOrUpdate(@NotNull PlayerData player);

    CompletableFuture<List<PlayerData>> getPlayers();

    List<PlayerData> getPlayersCached();
}
