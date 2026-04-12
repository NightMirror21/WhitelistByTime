package ru.nightmirror.wlbytime.filter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.PlayerKey;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class PlayerQuitFreezeListener implements Listener {

    SettingsConfig settingsConfig;
    EntryFinder entryFinder;
    EntryService entryService;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!settingsConfig.isFreezeCountsOnlyOnlineTime()) {
            return;
        }

        PlayerKey key = PlayerKey.uuid(event.getPlayer().getUniqueId());
        Optional<EntryImpl> entry = entryFinder.find(key);

        if (entry.isEmpty()) {
            key = PlayerKey.nickname(event.getPlayer().getName());
            entry = entryFinder.find(key);
        }

        entry.ifPresent(e -> {
            if (e.isFreezeActive() && !e.getFreezing().isPaused()) {
                log.debug("Pausing freeze for player={} on quit", event.getPlayer().getName());
                entryService.pauseFreeze(e);
            }
        });
    }

    public void unregister() {
        PlayerQuitEvent.getHandlerList().unregister(this);
    }
}
