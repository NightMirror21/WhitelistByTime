package ru.nightmirror.wlbytime.filter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.ResolvedPlayer;
import ru.nightmirror.wlbytime.interfaces.checker.AccessEntryChecker;
import ru.nightmirror.wlbytime.interfaces.checker.UnfreezeEntryChecker;
import ru.nightmirror.wlbytime.interfaces.identity.PlayerIdentityResolver;
import ru.nightmirror.wlbytime.interfaces.services.EntryIdentityService;
import ru.nightmirror.wlbytime.utils.ColorsUtils;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class PlayerLoginFilter implements Listener {

    MessagesConfig messagesConfig;
    SettingsConfig settingsConfig;
    UnfreezeEntryChecker unfreezeEntryChecker;
    AccessEntryChecker accessEntryChecker;
    PlayerIdentityResolver identityResolver;
    EntryIdentityService identityService;

    @EventHandler(priority = EventPriority.LOW)
    private void filter(AsyncPlayerPreLoginEvent event) {
        ResolvedPlayer resolved = identityResolver.resolveByLogin(event.getName(), event.getUniqueId());
        Optional<EntryImpl> entry = identityService.findOrMigrate(resolved, event.getName());
        entry.ifPresent(unfreezeEntryChecker::unfreezeIfRequired);
        if (!settingsConfig.isWhitelistEnabled()) {
            return;
        }
        boolean allowedLogin = entry.isPresent() && accessEntryChecker.isAllowed(entry.get());
        if (!allowedLogin) {
            log.debug("Login denied for nickname={} uuid={}", event.getName(), event.getUniqueId());
            Component component = ColorsUtils.convertMessage(messagesConfig.getYouNotInWhitelistOrFrozenKick());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, component);
        }
    }

    public void unregister() {
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
    }
}
