package ru.nightmirror.wlbytime.interfaces.database;

import java.util.List;

public interface IDatabase {

    void reload();
    void addPlayer(String nickname, long until);
    Boolean checkPlayer(String nickname);
    long getUntil(String nickname);
    void setUntil(String nickname, long until);
    void removePlayer(String nickname);
    List<String> getAll();
}
