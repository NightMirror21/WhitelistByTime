package ru.nightmirror.wlbytime.common.listeners;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.database.misc.PlayerData;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerLoginListener implements Listener {

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
                if (player.getUntil() != -1L && player.getUntil() <= System.currentTimeMillis()) {
                    List<String> message = ColorsConvertor.convert(plugin.getMessages().youNotInWhitelistKick);
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, String.join("\n", message));
                }
            }, () -> {
                List<String> message = ColorsConvertor.convert(plugin.getMessages().youNotInWhitelistKick);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, String.join("\n", message));
            });
        }).join();
    }

    @EventHandler
    private void loadToCache(PlayerJoinEvent event) {
        playerAccessor.loadPlayerToCache(event.getPlayer().getName());
    }
}
