package ru.nightmirror.wlbytime.interfaces.database;

import java.util.concurrent.CompletableFuture;

public interface Database {
    CompletableFuture<Boolean> reconnect();
    CompletableFuture<Boolean> close();
    CompletableFuture<Void> refreshCache();
}
