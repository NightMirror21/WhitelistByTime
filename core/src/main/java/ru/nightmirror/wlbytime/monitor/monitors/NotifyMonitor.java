package ru.nightmirror.wlbytime.monitor.monitors;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.ConfigsContainer;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;
import ru.nightmirror.wlbytime.monitor.Monitor;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.time.Duration;
import java.util.function.BiConsumer;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotifyMonitor extends Monitor {

    SettingsConfig settingsConfig;
    BiConsumer<String, String> messageSender;
    MessagesConfig messagesConfig;
    TimeConvertor timeConvertor;

    public NotifyMonitor(EntryDaoImpl dao, ConfigsContainer configsContainer, BiConsumer<String, String> messageSender) {
        super(dao, configsContainer.getSettings().isNotifyPlayersHowMuchLeft(), configsContainer.getSettings().getNotifyPlayerMonitorIntervalMs());
        this.settingsConfig = configsContainer.getSettings();
        this.messageSender = messageSender;
        this.messagesConfig = configsContainer.getMessages();
        timeConvertor = new TimeConvertor(configsContainer.getTimeUnitsConvertorSettings());
    }

    @Override
    protected void run() {
        long thresholdSeconds = settingsConfig.getNotifyPlayerTimeLeftThresholdSeconds();
        if (thresholdSeconds <= 0) {
            return;
        }
        dao.getAll().stream().filter(entry -> {
            if (entry.isForever() || entry.isFrozen()) {
                return false;
            }
            Duration left = entry.getLeftActiveDuration();
            return !left.isNegative() && left.getSeconds() <= thresholdSeconds;
        }).forEach(entry -> {
            String leftTime = timeConvertor.getTimeLine(entry.getLeftActiveDuration());
            String message = messagesConfig.getTimeLeftInWhitelistNotify().replace("%time%", leftTime);
            messageSender.accept(entry.getNickname(), message);
        });
    }
}
