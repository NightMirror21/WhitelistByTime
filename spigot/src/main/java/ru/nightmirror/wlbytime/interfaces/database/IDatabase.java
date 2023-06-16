package ru.nightmirror.wlbytime.interfaces.database;

import java.util.List;
import java.util.Map;

public interface IDatabase {

    void reload();
    void addPlayer(String nickname, long until);
    Boolean checkPlayer(String nickname);
    Boolean checkPlayer(long until);
    long getUntil(String nickname);
    void setUntil(String nickname, long until);
    void removePlayer(String nickname);
    Map<String, Long> getAll();
}
