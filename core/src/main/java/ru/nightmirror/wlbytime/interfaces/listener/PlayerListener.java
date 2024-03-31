package ru.nightmirror.wlbytime.interfaces.listener;

import ru.nightmirror.wlbytime.common.database.misc.PlayerData;

public interface PlayerListener {
    void playerRemoved(PlayerData player);
}
