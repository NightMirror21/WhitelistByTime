package ru.nightmirror.wlbytime.common.listeners;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.database.misc.PlayerData;
import ru.nightmirror.wlbytime.common.utils.ComponentUtils;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;
import ru.nightmirror.wlbytime.interfaces.listener.EventListener;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerLoginListener implements EventListener {

    PlayerAccessor playerAccessor;
    boolean caseSensitive;
    WhitelistByTime plugin;

    @EventHandler
    private void allowOrDisallow(AsyncPlayerPreLoginEvent event) {
        if (!plugin.isWhitelistEnabled()) return;

        playerAccessor.getPlayers().thenAccept(players -> {
            Optional<PlayerData> playerOptional = players
                    .stream()
                    .filter(player -> (caseSensitive && player.getNickname().equals(event.getName()) || (!caseSensitive && player.getNickname().equalsIgnoreCase(event.getName()))))
                    .findAny();

            playerOptional.ifPresentOrElse(player -> {
                if (player.calculateUntil() != -1L && player.calculateUntil() <= System.currentTimeMillis()) {
                    List<Component> message = ColorsConvertor.convert(plugin.getMessages().youNotInWhitelistKick);
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ComponentUtils.join(message, Component.text("\n")));
                }
            }, () -> {
                List<Component> message = ColorsConvertor.convert(plugin.getMessages().youNotInWhitelistKick);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ComponentUtils.join(message, Component.text("\n")));
            });
        }).join();
    }

    @EventHandler
    private void loadToCache(PlayerJoinEvent event) {
        playerAccessor.loadPlayerToCache(event.getPlayer().getName());
    }

    @Override
    public void unregister() {
        PlayerJoinEvent.getHandlerList().unregister(this);
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
    }
}
