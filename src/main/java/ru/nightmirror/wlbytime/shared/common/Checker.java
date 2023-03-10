package ru.nightmirror.wlbytime.shared.common;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.nightmirror.wlbytime.interfaces.database.IDatabase;
import ru.nightmirror.wlbytime.misc.convertors.ColorsConvertor;
import ru.nightmirror.wlbytime.shared.WhitelistByTime;
import ru.nightmirror.wlbytime.shared.api.events.PlayerRemovedFromWhitelistEvent;

import java.util.*;

@RequiredArgsConstructor
public class Checker {

    private final WhitelistByTime plugin;
    private final IDatabase database;
    public static Map<String, Long> players = new HashMap<String, Long>();
    final public static List<String> toKick = new ArrayList<>();

    private String casedName(String nickname) {
        if (!plugin.getConfig().getBoolean("case-sensitive", true)) {
            nickname = nickname.toLowerCase(Locale.ROOT);
        }
        return nickname;
    }

    private void check() {
        Map<String, Long> all = database.getAll();
        synchronized (players) {
            players = all;
        }

        for (Map.Entry<String, Long> playerEntry: all.entrySet()) {
            if (!database.checkPlayer(playerEntry.getValue())) {
                database.removePlayer(playerEntry.getKey());
            }
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            String name = casedName(player.getName());
            if(!all.containsKey(name)) {
                synchronized (toKick) {
                    toKick.add(casedName(name));
                }
            }
        }
    }

    private void kick(String nickname) {
        if (plugin.isWhitelistEnabled()) {
            Player player = plugin.getServer().getPlayer(nickname);
            if (player != null && player.isOnline()) {
                List<String> message = ColorsConvertor.convert(plugin.getConfig().getStringList("minecraft-commands.you-not-in-whitelist-kick"));
                player.kickPlayer(String.join("\n", message));
            }
        }
    }

    public BukkitTask start(final int delaySeconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (toKick) {
                    for (String player : toKick) {
                        PlayerRemovedFromWhitelistEvent event = new PlayerRemovedFromWhitelistEvent(player);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) return;

                        kick(player);
                    }

                    toKick.removeAll(toKick);
                }
            }
        }.runTaskTimer(plugin, 20L * delaySeconds, 20L * delaySeconds);
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::check, 20L * delaySeconds, 20L * delaySeconds);
    }
}
