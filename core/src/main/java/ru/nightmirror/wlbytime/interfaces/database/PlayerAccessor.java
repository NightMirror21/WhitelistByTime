package ru.nightmirror.wlbytime.interfaces.database;

import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.common.database.misc.WLPlayer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PlayerAccessor {
    CompletableFuture<Optional<WLPlayer>> getPlayer(@NotNull String nickname);
    Optional<WLPlayer> getPlayerCached(@NotNull String nickname);
    void loadPlayerToCache(@NotNull String nickname);
    void loadPlayersToCache(@NotNull List<String> nicknames);
    CompletableFuture<Boolean> createOrUpdate(@NotNull WLPlayer player);
    CompletableFuture<Boolean> delete(@NotNull WLPlayer player);
    CompletableFuture<Boolean> delete(@NotNull String nickname);
    CompletableFuture<Void> delete(@NotNull List<WLPlayer> players);
    CompletableFuture<List<WLPlayer>> getPlayers();
    List<WLPlayer> getPlayersCached();
}
