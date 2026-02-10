package ru.nightmirror.wlbytime.monitor.monitors;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;
import ru.nightmirror.wlbytime.monitor.Monitor;

import java.time.Instant;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LastJoinMonitor extends Monitor {

    private static final Logger LOGGER = Logger.getLogger(LastJoinMonitor.class.getSimpleName());

    long lastJoinThresholdMs;

    public LastJoinMonitor(EntryDaoImpl dao, SettingsConfig settings) {
        super(dao, settings.isLastJoinMonitorEnabled(), settings.getLastJoinMonitorIntervalMs());
        lastJoinThresholdMs = settings.getLastJoinExpirationThresholdSeconds() * 1000L;
    }

    @Override
    protected void run() {
        dao.getAll().stream().filter(entry -> {
            if (entry.getLastJoin() != null) {
                Instant lastJoinInstant = entry.getLastJoin().getLastJoinTime();
                return lastJoinInstant.isBefore(Instant.now().minusMillis(lastJoinThresholdMs));
            }
            return false;
        }).forEach(entry -> {
            dao.remove(entry);
            LOGGER.info(String.format("Entry with id %d (nickname: %s) has expired and was removed from database", entry.getId(), entry.getNickname()));
        });
    }
}
