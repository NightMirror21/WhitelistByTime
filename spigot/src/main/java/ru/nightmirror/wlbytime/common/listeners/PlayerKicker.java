package ru.nightmirror.wlbytime.common.listeners;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.database.misc.WLPlayer;
import ru.nightmirror.wlbytime.common.utils.BukkitSyncer;
import ru.nightmirror.wlbytime.interfaces.IWhitelist;
import ru.nightmirror.wlbytime.interfaces.listener.PlayerListener;
import ru.nightmirror.wlbytime.interfaces.misc.PlayersOnSeverAccessor;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerKicker implements PlayerListener, PlayersOnSeverAccessor {

    BukkitSyncer syncer;
    IWhitelist whitelist;
    boolean caseSensitive;
    List<String> kickMessage;

    @Override
    public void playerRemoved(WLPlayer wlPlayer) {
        kickPlayer(wlPlayer.getNickname());
    }

    @Override
    public List<String> getPlayersOnServer() {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();
    }

    @Override
    public void kickPlayer(@NotNull String nickname) {
        if (!whitelist.isWhitelistEnabled()) return;

        Bukkit.getOnlinePlayers().forEach(player -> {
            boolean toKick = (caseSensitive && player.getName().equals(nickname) || (!caseSensitive && player.getName().equalsIgnoreCase(nickname)));
            if (toKick) {
                syncer.sync(() -> {
                    List<String> message = ColorsConvertor.convert(kickMessage);
                    player.kickPlayer(String.join("\n", message));
                });
            }
        });
    }

    @Override
    public boolean isCaseSensitiveEnabled() {
        return caseSensitive;
    }
}
