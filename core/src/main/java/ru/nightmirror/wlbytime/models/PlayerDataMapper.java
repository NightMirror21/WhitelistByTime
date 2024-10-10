package ru.nightmirror.wlbytime.models;

import ru.nightmirror.wlbytime.interfaces.database.Mapper;

public class PlayerDataMapper implements Mapper<PlayerDataTable, PlayerData> {
    @Override
    public PlayerData toEntity(PlayerDataTable table) {
        return new PlayerData(table.getId(), table.getNickname(), table.getUntil(), table.getFrozenAt());
    }

    @Override
    public PlayerDataTable toTable(PlayerData entity) {
        return new PlayerDataTable(entity.getId(), entity.getNickname(), entity.getUntil(), entity.getFrozenAt());
    }
}
