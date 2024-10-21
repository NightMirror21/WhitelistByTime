package ru.nightmirror.wlbytime.syncer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class BukkitSyncer {

    Plugin plugin;

    public BukkitTask sync(Runnable runnable) {
        return plugin.getServer().getScheduler().runTask(plugin, runnable);
    }
}
