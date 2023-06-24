package ru.nightmirror.wlbytime.common.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.nightmirror.wlbytime.interfaces.IPlugin;
import ru.nightmirror.wlbytime.interfaces.database.IDatabase;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;

import java.util.List;

@RequiredArgsConstructor
public class PlayerLoginListener implements Listener {

    private final IDatabase database;
    private final IPlugin plugin;

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent event) {
        if (plugin.isWhitelistEnabled() && !database.checkPlayer(event.getName())) {
            List<String> message = ColorsConvertor.convert(plugin.getPluginConfig().getStringList("minecraft-commands.you-not-in-whitelist-kick"));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, String.join("\n", message));
        }
    }
}
