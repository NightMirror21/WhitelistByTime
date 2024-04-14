package ru.nightmirror.wlbytime.interfaces.database;

import java.util.concurrent.CompletableFuture;

public interface Database {

    CompletableFuture<Boolean> close();
}
