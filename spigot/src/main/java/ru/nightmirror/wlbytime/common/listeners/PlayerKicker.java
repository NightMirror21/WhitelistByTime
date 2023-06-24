package ru.nightmirror.wlbytime.common.listeners;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.database.misc.WLPlayer;
import ru.nightmirror.wlbytime.common.utils.BukkitSyncer;
import ru.nightmirror.wlbytime.interfaces.listener.PlayerListener;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerKicker implements PlayerListener {

    BukkitSyncer syncer;
    List<String> kickMessage;

    @Override
    public void playerRemoved(WLPlayer wlPlayer) {
        Player player = Bukkit.getPlayer(wlPlayer.getNickname());
        if (player != null && player.isOnline()) {
            syncer.sync(() -> {
                List<String> message = ColorsConvertor.convert(kickMessage);
                player.kickPlayer(String.join("\n", message));
            });
        }
    }
}
