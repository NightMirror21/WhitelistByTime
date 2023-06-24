package ru.nightmirror.wlbytime.common.checker;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.common.database.misc.WLPlayer;
import ru.nightmirror.wlbytime.interfaces.checker.Checker;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PlayersChecker implements Checker, Runnable {

    PlayerAccessor playerAccessor;
    Duration delay;
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void start() {
        executor.scheduleAtFixedRate(this, delay.toMillis(), delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        playerAccessor.getPlayers().thenAccept((players) -> {
            long currentMilliseconds = System.currentTimeMillis();
            List<WLPlayer> toRemove = players.stream().filter(player -> player.getUntil() <= currentMilliseconds).toList();
            playerAccessor.delete(toRemove).join();
        }).join();
    }

    @Override
    public void stop() {
        executor.shutdown();
    }
}
