package ru.nightmirror.wlbytime.database;

import java.util.List;

public interface IDatabase {
    void addPlayer(String nickname, long until);
    Boolean checkPlayer(String nickname);
    long getUntil(String nickname);
    void removePlayer(String nickname);
    List<String> getAll();
}
