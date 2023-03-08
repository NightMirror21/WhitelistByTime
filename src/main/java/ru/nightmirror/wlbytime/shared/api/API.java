package ru.nightmirror.wlbytime.shared.api;

import lombok.RequiredArgsConstructor;
import ru.nightmirror.wlbytime.interfaces.api.IAPI;
import ru.nightmirror.wlbytime.interfaces.database.IDatabase;
import ru.nightmirror.wlbytime.misc.convertors.TimeConvertor;
import ru.nightmirror.wlbytime.shared.WhitelistByTime;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class API implements IAPI {

    private final IDatabase database;
    private final WhitelistByTime plugin;

    @Override
    public boolean addPlayer(String nickname, long until) {
        if (database.checkPlayer(nickname)) return false;
        database.addPlayer(nickname, until);
        return true;
    }

    @Override
    public boolean checkPlayer(String nickname) {
        return database.checkPlayer(nickname);
    }

    @Override
    public long getUntil(String nickname) {
        return database.getUntil(nickname);
    }

    @Override
    public String getUntilString(String nickname) {
        return TimeConvertor.getTimeLine(plugin, database.getUntil(nickname));
    }

    @Override
    public boolean removePlayer(String nickname) {
        if (!database.checkPlayer(nickname)) return false;
        database.removePlayer(nickname);
        return true;
    }

    @Override
    public Map<String, Long> getAllPlayers() {
        return database.getAll();
    }

    @Override
    public boolean setUntil(String nickname, long until) {
        if (!database.checkPlayer(nickname)) return false;
        database.setUntil(nickname, until);
        return true;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isWhitelistEnabled();
    }
}
