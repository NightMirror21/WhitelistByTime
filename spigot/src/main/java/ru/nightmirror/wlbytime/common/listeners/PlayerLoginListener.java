package ru.nightmirror.wlbytime.common.listeners;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.filters.ConnectingPlayersFilter;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerLoginListener implements Listener {

    WhitelistByTime plugin;
    PlayerAccessor playerAccessor;
    Predicate<ConnectingPlayersFilter.ConnectingPlayer> filter;

    @EventHandler
    private void allowOrDisallow(AsyncPlayerPreLoginEvent event) {
        if (!filter.test(new ConnectingPlayersFilter.ConnectingPlayer(event.getName(), event.getUniqueId()))) {
            List<String> message = ColorsConvertor.convert(plugin.getMessages().youNotInWhitelistKick);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, String.join("\n", message));
        }
    }

    @EventHandler
    private void loadToCache(PlayerJoinEvent event) {
        playerAccessor.loadPlayerToCache(event.getPlayer().getName());
    }
}
