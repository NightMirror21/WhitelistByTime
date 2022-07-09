package ru.nightmirror.wlbytime.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import ru.nightmirror.wlbytime.convertors.ColorsConvertor;
import ru.nightmirror.wlbytime.database.IDatabase;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final IDatabase database;
    private final Plugin plugin;

    @EventHandler
    private void onJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (!database.checkPlayer(player.getName())) {
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
            event.setKickMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.you-not-in-whitelist", "null")));
        }
    }
}
