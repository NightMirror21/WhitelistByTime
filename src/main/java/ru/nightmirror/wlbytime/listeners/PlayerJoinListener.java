package ru.nightmirror.wlbytime.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.nightmirror.wlbytime.main.Config;
import ru.nightmirror.wlbytime.main.SQLite;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!SQLite.getInstance().checkPlayer(player.getName())) {
            event.setJoinMessage(null);
            player.kickPlayer(Config.getInstance().getLine("you-not-in-whitelist"));
        }

    }
}
