package ru.nightmirror.wlbytime.syncer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.plugin.Plugin;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MainThreadSync {

    Plugin plugin;

    public void run(Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }
}
