package ru.nightmirror.wlbytime.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.wlbytime.listeners.PlayerJoinListener;
import ru.nightmirror.wlbytime.listeners.WhitelistCmdListener;
import ru.nightmirror.wlbytime.listeners.WhitelistTabCompleter;

import java.util.logging.Logger;

public class WhitelistByTime extends JavaPlugin {

    private final Logger log = Logger.getLogger("WhitelistByTime");

    @Override
    public void onEnable() {
        Config config = Config.getInstance();
        config.checkConfig(this);

        Database.getInstance().init(this);

        Bukkit.getPluginManager().registerEvents(new WhitelistCmdListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);

        getCommand("whitelist").setTabCompleter(new WhitelistTabCompleter());

        log.info(ChatColor.GREEN + "Enabled!");
    }

    @Override
    public void onDisable() {
        log.info(ChatColor.GOLD + "Disabled");
    }
}
