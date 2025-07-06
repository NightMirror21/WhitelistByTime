package ru.nightmirror.wlbytime.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.syncer.MainThreadSync;
import ru.nightmirror.wlbytime.utils.ColorsUtils;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerKicker implements Consumer<String> {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    MessagesConfig messagesConfig;
    MainThreadSync mainThreadSync;

    @Override
    public void accept(String nicknameOrUuid) {
        Player player;
        if (isUuid(nicknameOrUuid)) {
            player = Bukkit.getPlayer(UUID.fromString(nicknameOrUuid));
        } else {
            player = Bukkit.getPlayer(nicknameOrUuid);
        }

        if (player != null && player.isOnline()) {
            Component component = ColorsUtils.convertMessage(messagesConfig.getYouNotInWhitelistOrFrozenKick());
            mainThreadSync.run(() -> player.kick(component, PlayerKickEvent.Cause.WHITELIST));
        }
    }

    private static boolean isUuid(String raw) {
        return UUID_PATTERN.matcher(raw).matches();
    }
}
