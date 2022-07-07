package ru.nightmirror.wlbytime.api;

import ru.nightmirror.wlbytime.database.Database;
import ru.nightmirror.wlbytime.convertors.TimeConvertor;

import java.util.List;

public class WhitelistByTimeAPI {

    private static Database database = new Database();

    /*
    until (ms) - The time until which the player will be in the white list.
    -1 = the player will be on the white list forever
     */

    public static void addPlayer(String nickname, long until) {
        database.addPlayer(nickname, until);
    }

    public static Boolean checkPlayer(String nickname) {
        return database.checkPlayer(nickname);
    }

    public static long getUntil(String nickname) {
        return database.getUntil(nickname);
    }

    public static String getUntilString(String nickname) {
        return TimeConvertor.getTimeLine(database.getUntil(nickname) - System.currentTimeMillis());
    }

    public static void removePlayer(String nickname) {
        database.removePlayer(nickname);
    }

    public static List<String> getAllPlayers() {
        return database.getAll();
    }
}
