package ru.nightmirror.wlbytime.monitor.monitors;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;
import ru.nightmirror.wlbytime.monitor.Monitor;

import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LastJoinMonitor extends Monitor {

    private static final Logger LOGGER = Logger.getLogger(LastJoinMonitor.class.getSimpleName());

    public LastJoinMonitor(EntryDaoImpl dao, SettingsConfig settings) {
        super(dao, settings, settings.isLastJoinMonitorEnabled(), settings.getLastJoinMonitorIntervalMs());
    }

    @Override
    protected void run() {

    }
}
