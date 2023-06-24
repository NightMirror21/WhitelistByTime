package ru.nightmirror.wlbytime;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.nightmirror.wlbytime.interfaces.IPlugin;
import ru.nightmirror.wlbytime.interfaces.database.IDatabase;
import ru.nightmirror.wlbytime.common.listeners.PlayerLoginListener;
import ru.nightmirror.wlbytime.common.listeners.WhitelistCmdListener;
import ru.nightmirror.wlbytime.common.utils.ConfigUtils;
import ru.nightmirror.wlbytime.common.checker.Checker;
import ru.nightmirror.wlbytime.shared.database.Database;
import ru.nightmirror.wlbytime.common.command.CommandsExecutor;
import ru.nightmirror.wlbytime.common.command.WhitelistCommandExecutor;
import ru.nightmirror.wlbytime.common.command.WhitelistTabCompleter;
import ru.nightmirror.wlbytime.common.placeholder.PlaceholderHook;

import java.util.logging.Logger;

public class WhitelistByTime extends JavaPlugin implements IPlugin {

    private final Logger log = Logger.getLogger("WhitelistByTime");

    private boolean whitelistEnabled = true;

    private BukkitTask checker;

    @Override
    public void onEnable() {
        ConfigUtils.checkConfig(this);

        whitelistEnabled = getConfig().getBoolean("enabled", true);

        IDatabase database = new Database(this);

        Bukkit.getPluginManager().registerEvents(new WhitelistCmdListener(new CommandsExecutor(database, this)), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(database, this), this);

        getCommand("whitelist").setExecutor(new WhitelistCommandExecutor(new CommandsExecutor(database, this)));
        getCommand("wl").setExecutor(new WhitelistCommandExecutor(new CommandsExecutor(database, this)));
        getCommand("whitelist").setTabCompleter(new WhitelistTabCompleter(database, this));
        getCommand("wl").setTabCompleter(new WhitelistTabCompleter(database, this));

        if (getConfig().getBoolean("checker-thread", true)) {
            checker = new Checker(this, database).start(getConfig().getInt("checker-delay", 1));
        }

        new Metrics(this, 13834);

        if (getConfig().getBoolean("placeholders-enabled", false)) {
            try {
                new PlaceholderHook(database, this).register();
                log.info("Hooked with PlaceholderAPI");
            } catch (Exception exception) {
                log.warning("Can't hook with PlaceholderAPI. " + exception.getMessage());
            }
        }

        log.info("Enabled!");
    }

    @Override
    public void onDisable() {
        if (checker != null) {
            checker.cancel();
        }

        log.info(ChatColor.GOLD + "Disabled");
    }

    @Override
    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    @Override
    public void setWhitelistEnabled(boolean mode) {
        whitelistEnabled = mode;
    }

    @Override
    public FileConfiguration getPluginConfig() {
        return getConfig();
    }
}
