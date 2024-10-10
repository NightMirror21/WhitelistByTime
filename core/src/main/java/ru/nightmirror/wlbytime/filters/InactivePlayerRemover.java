package ru.nightmirror.wlbytime.filters;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.interfaces.database.PlayerDao;
import ru.nightmirror.wlbytime.interfaces.misc.PlayersOnSeverAccessor;
import ru.nightmirror.wlbytime.models.PlayerData;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InactivePlayerRemover implements Runnable {

    PlayerDao playerDao;
    PlayersOnSeverAccessor playersOnSeverAccessor;

    public InactivePlayerRemover(PlayerDao playerDao, PlayersOnSeverAccessor playersOnSeverAccessor) {
        this.playerDao = playerDao;
        this.playersOnSeverAccessor = playersOnSeverAccessor;
    }

    @Override
    public void run() {
        playerDao.getPlayers().thenAccept(players -> {
            List<PlayerData> toRemove = players.stream().filter(player -> !player.canPlay()).toList();
            List<String> onServer = playersOnSeverAccessor.getPlayersOnServer();

            boolean caseSensitive = playersOnSeverAccessor.isCaseSensitiveEnabled();

            if (caseSensitive) {
                for (String nickname : onServer) {
                    boolean toKick = players.stream().noneMatch(player -> player.getNickname().equals(nickname));
                    if (toKick) {
                        playersOnSeverAccessor.kickPlayer(nickname);
                    }
                }
            }

            for (PlayerData player : toRemove) {
                boolean toKick = onServer.stream().anyMatch(nickname -> (caseSensitive && player.getNickname().equals(nickname) || (!caseSensitive && player.getNickname().equalsIgnoreCase(nickname))));
                if (toKick) {
                    playersOnSeverAccessor.kickPlayer(player.getNickname());
                }
            }
        }).join();
    }
}
