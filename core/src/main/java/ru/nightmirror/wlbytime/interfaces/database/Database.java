package ru.nightmirror.wlbytime.interfaces.database;

import java.util.concurrent.CompletableFuture;

public interface Database {
    // TODO Unused :(
    CompletableFuture<Boolean> reconnect();

    CompletableFuture<Boolean> close();

    // TODO Unused :(
    CompletableFuture<Void> refreshCache();
}
