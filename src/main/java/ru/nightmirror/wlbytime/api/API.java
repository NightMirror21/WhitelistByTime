package ru.nightmirror.wlbytime.api;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import ru.nightmirror.wlbytime.convertors.TimeConvertor;
import ru.nightmirror.wlbytime.interfaces.database.IDatabase;
import ru.nightmirror.wlbytime.interfaces.api.IAPI;

import java.util.List;

@RequiredArgsConstructor
public class API implements IAPI {

    private final IDatabase database;
    private final Plugin plugin;

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
    public List<String> getAllPlayers() {
        return database.getAll();
    }

    @Override
    public boolean setUntil(String nickname, long until) {
        if (!database.checkPlayer(nickname)) return false;
        database.setUntil(nickname, until);
        return true;
    }
}
