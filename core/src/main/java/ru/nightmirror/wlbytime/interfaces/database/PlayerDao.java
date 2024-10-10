package ru.nightmirror.wlbytime.interfaces.database;

import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.models.PlayerData;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface PlayerDao {
    CompletableFuture<Optional<PlayerData>> getPlayer(@NotNull String nickname);

    Optional<PlayerData> getPlayerCached(@NotNull String nickname);

    void loadPlayerToCache(@NotNull String nickname);

    void loadPlayersToCache(@NotNull Set<String> nicknames);

    CompletableFuture<Boolean> createOrUpdate(@NotNull PlayerData player);

    CompletableFuture<Set<PlayerData>> getPlayers();

    Set<PlayerData> getPlayersCached();
}
