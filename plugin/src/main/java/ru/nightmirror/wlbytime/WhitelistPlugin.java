package ru.nightmirror.wlbytime;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.wlbytime.config.ConfigsContainer;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;
import ru.nightmirror.wlbytime.impl.service.EntryServiceImpl;
import ru.nightmirror.wlbytime.impl.service.EntryTimeServiceImpl;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;
import ru.nightmirror.wlbytime.monitor.Monitor;
import ru.nightmirror.wlbytime.monitor.monitors.ExpireMonitor;
import ru.nightmirror.wlbytime.monitor.monitors.LastJoinMonitor;

import java.util.logging.Level;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WhitelistPlugin extends JavaPlugin {

    EntryDaoImpl entryDao;
    Monitor expireMonitor;
    Monitor lastJoinMonitor;

    @Override
    public void onEnable() {
        try {
            tryToEnable();
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "Failed to enable plugin", exception);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void tryToEnable() {
        getLogger().info("Loading configs...");
        ConfigsContainer configsContainer = new ConfigsContainer(getDataFolder());
        configsContainer.load();
        getLogger().info("Configs loaded");

        getLogger().info("Loading database...");
        entryDao = new EntryDaoImpl(configsContainer.getDatabase());
        getLogger().info("Database loaded");

        getLogger().info("Initializing services...");
        EntryService entryService = new EntryServiceImpl(entryDao);
        EntryTimeService entryTimeService = new EntryTimeServiceImpl(entryDao);
        getLogger().info("Services initialized");

        getLogger().info("Starting monitors...");
        expireMonitor = new ExpireMonitor(entryDao, configsContainer.getSettings());
        lastJoinMonitor = new LastJoinMonitor(entryDao, configsContainer.getSettings());
        getLogger().info("Monitors started");

        // TODO load commands

        getLogger().info("Plugin enabled");
    }

    @Override
    public void onDisable() {
        try {
            tryToDisable();
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "Failed to disable plugin", exception);
        }
    }

    private void tryToDisable() {
        if (entryDao != null) {
            entryDao.close();
        }
        if (expireMonitor != null) {
            expireMonitor.shutdown();
        }
        if (lastJoinMonitor != null) {
            lastJoinMonitor.shutdown();
        }
        getLogger().info("Plugin disabled");
    }
}
