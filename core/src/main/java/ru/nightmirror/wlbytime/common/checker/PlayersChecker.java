package ru.nightmirror.wlbytime.common.checker;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.common.database.misc.WLPlayer;
import ru.nightmirror.wlbytime.interfaces.checker.Checker;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;
import ru.nightmirror.wlbytime.interfaces.misc.PlayersOnSeverAccessor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PlayersChecker implements Checker, Runnable {

    PlayerAccessor playerAccessor;
    PlayersOnSeverAccessor playersOnSeverAccessor;
    Duration delay;
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void start() {
        System.out.println("Player checker started");
        executor.scheduleAtFixedRate(this, delay.toMillis(), delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        playerAccessor.getPlayers().thenCompose(players -> {
            System.out.println("Players on server: " + players.toString());

            long currentMilliseconds = System.currentTimeMillis();
            List<WLPlayer> toRemove = players.stream().filter(player -> player.getUntil() != -1L && player.getUntil() <= currentMilliseconds).toList();
            List<String> onServer = playersOnSeverAccessor.getPlayersOnServer();

            boolean caseSensitive = playersOnSeverAccessor.isCaseSensitiveEnabled();

            if (caseSensitive) {
                for (String nickname : onServer) {
                    boolean toKick = players.stream().noneMatch(player -> player.getNickname().equals(nickname));
                    if (toKick) {
                        System.out.println("PlayerChecker. Kicking case sensitive " + nickname);
                        playersOnSeverAccessor.kickPlayer(nickname);
                    }
                }
            }

            for (WLPlayer player : toRemove) {
                boolean toKick = onServer.stream().anyMatch(nickname -> (caseSensitive && player.getNickname().equals(nickname) || (!caseSensitive && player.getNickname().equalsIgnoreCase(nickname))));
                if (toKick) {
                    System.out.println("PlayerChecker. Kicking " + player.getNickname());
                    playersOnSeverAccessor.kickPlayer(player.getNickname());
                } else {
                    System.out.println("PlayerChecker. Player not found " + player.getNickname());
                }
            }

            return playerAccessor.delete(toRemove);
        });
    }

    @Override
    public void stop() {
        System.out.println("Player checker stopped");
        executor.shutdown();
    }
}
