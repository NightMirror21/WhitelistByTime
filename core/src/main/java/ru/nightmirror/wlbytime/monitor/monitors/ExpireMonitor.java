package ru.nightmirror.wlbytime.monitor.monitors;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;
import ru.nightmirror.wlbytime.monitor.Monitor;

import java.util.function.Consumer;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExpireMonitor extends Monitor {

    private static final Logger LOGGER = Logger.getLogger(ExpireMonitor.class.getSimpleName());

    Consumer<String> playerKicker;
    boolean kickPlayerOnExpire;

    public ExpireMonitor(EntryDaoImpl dao, SettingsConfig settings, Consumer<String> playerKicker) {
        super(dao, settings.isExpireMonitorEnabled(), settings.getExpireMonitorIntervalMs());
        this.playerKicker = playerKicker;
        this.kickPlayerOnExpire = settings.isKickPlayerOnTimeExpire();
    }

    @Override
    protected void run() {
        dao.getAll().stream().filter(EntryImpl::isInactive).forEach(entry -> {
            dao.remove(entry);
            LOGGER.info(String.format("Entry with id %d (nickname: %s) has expired and was removed from database", entry.getId(), entry.getNickname()));
            if (kickPlayerOnExpire) {
                playerKicker.accept(entry.getNickname());
            }
        });
    }
}
