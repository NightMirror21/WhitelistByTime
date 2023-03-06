package ru.nightmirror.wlbytime.shared.common;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.nightmirror.wlbytime.interfaces.database.IDatabase;
import ru.nightmirror.wlbytime.shared.WhitelistByTime;

@RequiredArgsConstructor
public class Checker {

    private final WhitelistByTime plugin;
    private final IDatabase database;

    public BukkitTask start(final int delaySeconds) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, database::getAll, 20L * delaySeconds, 20L * delaySeconds);
    }
}
