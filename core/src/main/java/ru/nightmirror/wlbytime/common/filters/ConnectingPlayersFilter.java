package ru.nightmirror.wlbytime.common.filters;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

import java.util.UUID;
import java.util.function.Predicate;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectingPlayersFilter implements Predicate<ConnectingPlayersFilter.ConnectingPlayer> {

    PlayerAccessor playerAccessor;
    boolean caseSensitive;
    WhitelistByTime plugin;

    @Override
    public boolean test(ConnectingPlayer connectingPlayer) {
        if (!plugin.isWhitelistEnabled()) return true;

        return playerAccessor.getPlayer(connectingPlayer.getNickname())
                .thenApply(playerOptional -> playerOptional.map(player -> {
                    if (player.calculateUntil() != -1L && player.calculateUntil() <= System.currentTimeMillis()) {
                        return false;
                    } else if (player.isFrozen() && plugin.getPluginConfig().unfreezeOnJoin) {
                        player.switchFreeze();
                        playerAccessor.createOrUpdate(player);
                    }
                    return true;
                }).orElse(false)).join();
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    public static class ConnectingPlayer {
        String nickname;
        UUID uuid; // for future
    }
}
