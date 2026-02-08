package ru.nightmirror.wlbytime.filter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.checker.AccessEntryChecker;
import ru.nightmirror.wlbytime.interfaces.checker.UnfreezeEntryChecker;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.utils.ColorsUtils;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PlayerLoginFilter implements Listener {

    MessagesConfig messagesConfig;
    SettingsConfig settingsConfig;
    EntryFinder entryFinder;
    UnfreezeEntryChecker unfreezeEntryChecker;
    AccessEntryChecker accessEntryChecker;

    @EventHandler(priority = EventPriority.LOW)
    private void filter(AsyncPlayerPreLoginEvent event) {
        Optional<EntryImpl> entry = entryFinder.find(event.getName());
        entry.ifPresent(unfreezeEntryChecker::unfreezeIfRequired);
        if (!settingsConfig.isWhitelistEnabled()) {
            return;
        }
        boolean allowedLogin = entry.isPresent() && accessEntryChecker.isAllowed(entry.get());
        if (!allowedLogin) {
            Component component = ColorsUtils.convertMessage(messagesConfig.getYouNotInWhitelistOrFrozenKick());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, component);
        }
    }

    public void unregister() {
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
    }
}
