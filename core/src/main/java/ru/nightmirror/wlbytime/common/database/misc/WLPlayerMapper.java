package ru.nightmirror.wlbytime.common.database.misc;

import ru.nightmirror.wlbytime.interfaces.database.Mapper;

public class WLPlayerMapper implements Mapper<WLPlayerTable, WLPlayer> {
    @Override
    public WLPlayer toEntity(WLPlayerTable table) {
        return new WLPlayer(table.getId(), table.getNickname(), table.getUntil());
    }

    @Override
    public WLPlayerTable toTable(WLPlayer entity) {
        return new WLPlayerTable(entity.getId(), entity.getNickname(), entity.getUntil());
    }
}
