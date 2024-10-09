package ru.nightmirror.wlbytime.common.filters;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.database.PlayerDao;

import java.util.UUID;
import java.util.function.Predicate;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectingPlayersFilter implements Predicate<ConnectingPlayersFilter.ConnectingPlayer> {

    PlayerDao playerDao;
    boolean caseSensitive;
    WhitelistByTime plugin;

    @Override
    public boolean test(ConnectingPlayer connectingPlayer) {
        if (!plugin.isWhitelistEnabled()) return true;

        return playerDao.getPlayer(connectingPlayer.getNickname())
                .thenApply(playerOptional -> playerOptional.map(player -> {
                    if (!player.canPlay()) {
                        return false;
                    } else if (player.isFrozen() && plugin.getPluginConfig().isUnfreezeOnJoin()) {
                        player.switchFreeze();
                        playerDao.createOrUpdate(player);
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
