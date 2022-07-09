package ru.nightmirror.wlbytime.main;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.A;
import ru.nightmirror.wlbytime.api.API;
import ru.nightmirror.wlbytime.api.IAPI;
import ru.nightmirror.wlbytime.config.ConfigUtils;
import ru.nightmirror.wlbytime.database.IDatabase;
import ru.nightmirror.wlbytime.listeners.PlayerJoinListener;
import ru.nightmirror.wlbytime.listeners.WhitelistCmdListener;
import ru.nightmirror.wlbytime.listeners.WhitelistTabCompleter;

import java.util.logging.Logger;

public class WhitelistByTime extends JavaPlugin {

    private final Logger log = Logger.getLogger("WhitelistByTime");
    private IDatabase database;
    private static IAPI api;

    private BukkitTask checker;

    @Override
    public void onEnable() {
        ConfigUtils.checkConfig(this);

        Bukkit.getPluginManager().registerEvents(new WhitelistCmdListener(database, this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(database, this), this);

        getCommand("whitelist").setTabCompleter(new WhitelistTabCompleter(database, this));

        if (getConfig().getBoolean("checker-thread", true)) {
            checker = new Checker(this, database).start(getConfig().getInt("checker-thread", 1));
        }

        api = new API(database, this);

        Metrics metrics = new Metrics(this, 13834);

        log.info(ChatColor.GREEN + "Enabled!");
    }

    @Override
    public void onDisable() {
        if (checker != null) {
            checker.cancel();
        }

        log.info(ChatColor.GOLD + "Disabled");
    }

    public static IAPI getAPI() {
        return api;
    }
}
