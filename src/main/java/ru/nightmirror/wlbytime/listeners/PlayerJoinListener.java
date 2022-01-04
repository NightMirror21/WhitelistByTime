package ru.nightmirror.wlbytime.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import ru.nightmirror.wlbytime.main.Config;
import ru.nightmirror.wlbytime.main.Database;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (!Database.getInstance().checkPlayer(player.getName())) {
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
            event.setKickMessage(Config.getInstance().getLine("you-not-in-whitelist"));
        }
    }
}
