package ru.nightmirror.wlbytime.interfaces.database;

import java.util.concurrent.CompletableFuture;

public interface CachedDatabase {
    CompletableFuture<Boolean> reconnect();
    CompletableFuture<Boolean> close();
    CompletableFuture<Void> refreshCache();
}
