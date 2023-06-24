package ru.nightmirror.wlbytime.common.listeners;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.interfaces.IWhitelist;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;
import ru.nightmirror.wlbytime.interfaces.listener.EventListener;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerLoginListener implements EventListener {

    PlayerAccessor playerAccessor;
    IWhitelist plugin;

    @EventHandler
    private void allowOrDisallow(AsyncPlayerPreLoginEvent event) {
        if (!plugin.isWhitelistEnabled()) return;

        playerAccessor.getPlayer(event.getName()).thenAccept(playerOptional -> {
            playerOptional.ifPresentOrElse(player -> {
                if (player.getUntil() <= System.currentTimeMillis()) {
                    List<String> message = ColorsConvertor.convert(plugin.getPluginConfig().getStringList("minecraft-commands.you-not-in-whitelist-kick"));
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, String.join("\n", message));
                }
            }, () -> {
                List<String> message = ColorsConvertor.convert(plugin.getPluginConfig().getStringList("minecraft-commands.you-not-in-whitelist-kick"));
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, String.join("\n", message));
            });
        }).join();
    }

    @EventHandler
    private void loadToCache(PlayerJoinEvent event) {
        playerAccessor.loadPlayerToCache(event.getPlayer().getName());
    }

    @Override
    public void unregister() {
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
    }
}
