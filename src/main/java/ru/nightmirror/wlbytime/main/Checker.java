package ru.nightmirror.wlbytime.main;

import org.bukkit.entity.Player;
import ru.nightmirror.wlbytime.database.Database;

public class Checker implements Runnable {

    private static Boolean flag = true;
    private final WhitelistByTime plugin;
    private final Database database = Database.getInstance();

    public Checker(WhitelistByTime plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        while (flag) {
            try { Thread.sleep(1000); } catch (Exception exception) { }
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                database.checkPlayer(player.getName());
            }
        }
    }

    public static void stop() {
        flag = false;
    }
}
