package ru.nightmirror.wlbytime.common.listeners;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.utils.BukkitSyncer;
import ru.nightmirror.wlbytime.common.utils.ComponentUtils;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.listener.PlayerListener;
import ru.nightmirror.wlbytime.interfaces.misc.PlayersOnSeverAccessor;
import ru.nightmirror.wlbytime.models.PlayerData;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerKicker implements PlayerListener, PlayersOnSeverAccessor {

    BukkitSyncer syncer;
    WhitelistByTime whitelistByTime;
    boolean caseSensitive;
    List<String> kickMessage;

    @Override
    public void playerRemoved(PlayerData playerData) {
        kickPlayer(playerData.getNickname());
    }

    @Override
    public List<String> getPlayersOnServer() {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();
    }

    @Override
    public void kickPlayer(@NotNull String nickname) {
        if (!whitelistByTime.isWhitelistEnabled()) return;

        Bukkit.getOnlinePlayers().forEach(player -> {
            boolean toKick = (caseSensitive && player.getName().equals(nickname) || (!caseSensitive && player.getName().equalsIgnoreCase(nickname)));
            if (toKick) {
                syncer.sync(() -> {
                    List<Component> message = ColorsConvertor.convert(kickMessage);
                    player.kick(ComponentUtils.join(message, Component.text("\n")), PlayerKickEvent.Cause.WHITELIST);
                });
            }
        });
    }

    @Override
    public boolean isCaseSensitiveEnabled() {
        return caseSensitive;
    }
}
