package ru.nightmirror.wlbytime.monitor;

import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class Monitor {

    private static final Logger LOGGER = Logger.getLogger(Monitor.class.getSimpleName());

    protected final EntryDaoImpl dao;
    protected final SettingsConfig settings;

    private final ScheduledExecutorService executor;

    public Monitor(EntryDaoImpl dao, SettingsConfig settings, boolean enabled, int intervalMs) {
        this.dao = dao;
        this.settings = settings;

        if (enabled) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(this::run, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        } else {
            executor = null;
        }
    }

    protected abstract void run();

    public void shutdown() {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
