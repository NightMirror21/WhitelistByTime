package ru.nightmirror.wlbytime.main;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.nightmirror.wlbytime.database.Database;
import ru.nightmirror.wlbytime.database.IDatabase;

@RequiredArgsConstructor
public class Checker {

    private final WhitelistByTime plugin;
    private final IDatabase database;

    public BukkitTask start(final int delaySeconds) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    database.checkPlayer(player.getName());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L * delaySeconds, 20L * delaySeconds);
    }
}
