package ru.nightmirror.wlbytime.common.listeners;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.filters.ConnectingPlayersFilter;
import ru.nightmirror.wlbytime.common.utils.ComponentUtils;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.database.PlayerDao;
import ru.nightmirror.wlbytime.interfaces.listener.EventListener;

import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerLoginListener implements EventListener {

    WhitelistByTime plugin;
    PlayerDao playerDao;
    Predicate<ConnectingPlayersFilter.ConnectingPlayer> filter;

    @EventHandler
    private void allowOrDisallow(AsyncPlayerPreLoginEvent event) {
        if (!filter.test(new ConnectingPlayersFilter.ConnectingPlayer(event.getName(), event.getUniqueId()))) {
            List<Component> message = ColorsConvertor.convert(plugin.getMessages().getYouNotInWhitelistKick());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ComponentUtils.join(message, Component.text("\n")));
        }
    }

    @EventHandler
    private void loadToCache(PlayerJoinEvent event) {
        playerDao.loadPlayerToCache(event.getPlayer().getName());
    }

    @Override
    public void unregister() {
        PlayerJoinEvent.getHandlerList().unregister(this);
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
    }
}
