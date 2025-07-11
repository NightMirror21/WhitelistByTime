package ru.nightmirror.wlbytime;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.wlbytime.command.CommandDispatcher;
import ru.nightmirror.wlbytime.command.CommandProxy;
import ru.nightmirror.wlbytime.command.CommandsLoader;
import ru.nightmirror.wlbytime.config.ConfigsContainer;
import ru.nightmirror.wlbytime.consumers.MessageSender;
import ru.nightmirror.wlbytime.consumers.PlayerKicker;
import ru.nightmirror.wlbytime.filter.PlayerLoginFilter;
import ru.nightmirror.wlbytime.impl.checker.AccessEntryCheckerImpl;
import ru.nightmirror.wlbytime.impl.checker.UnfreezeEntryCheckerImpl;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;
import ru.nightmirror.wlbytime.impl.finder.EntryFinderImpl;
import ru.nightmirror.wlbytime.impl.parser.PlaceholderParserImpl;
import ru.nightmirror.wlbytime.impl.service.EntryServiceImpl;
import ru.nightmirror.wlbytime.impl.service.EntryTimeServiceImpl;
import ru.nightmirror.wlbytime.interfaces.checker.AccessEntryChecker;
import ru.nightmirror.wlbytime.interfaces.checker.UnfreezeEntryChecker;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.parser.PlaceholderParser;
import ru.nightmirror.wlbytime.interfaces.plugin.Reloadable;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;
import ru.nightmirror.wlbytime.monitor.Monitor;
import ru.nightmirror.wlbytime.monitor.monitors.ExpireMonitor;
import ru.nightmirror.wlbytime.monitor.monitors.LastJoinMonitor;
import ru.nightmirror.wlbytime.monitor.monitors.NotifyMonitor;
import ru.nightmirror.wlbytime.placeholder.PlaceholderHookProxy;
import ru.nightmirror.wlbytime.syncer.MainThreadSync;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;
import ru.nightmirror.wlbytime.time.TimeUnitsConvertorSettings;
import ru.nightmirror.wlbytime.utils.MetricsUtils;
import ru.nightmirror.wlbytime.utils.VersionUtils;

import java.util.Set;
import java.util.logging.Level;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WhitelistPlugin extends JavaPlugin implements Reloadable {

    static final Set<String> WHITELIST_COMMANDS = Set.of("whitelist", "wl", "wlbytime", "whitelistbytime");

    EntryDaoImpl entryDao;
    Monitor expireMonitor;
    Monitor lastJoinMonitor;
    Monitor notifyMonitor;

    String version;
    ConfigsContainer configsContainer;
    TimeConvertor timeConvertor;
    EntryFinder entryFinder;

    PlayerLoginFilter playerLoginFilter;

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
        version = VersionUtils.getVersion(this);
        getLogger().info("Version " + version);

        getLogger().info("Loading configs...");
        configsContainer = new ConfigsContainer(getDataFolder());
        configsContainer.load();
        getLogger().info("Configs loaded");

        getLogger().info("Loading database...");
        entryDao = new EntryDaoImpl(getDataFolder(), configsContainer.getDatabase());
        entryFinder = new EntryFinderImpl(configsContainer.getSettings().isNicknameCaseSensitive(), entryDao);
        getLogger().info("Database loaded");

        getLogger().info("Initializing services...");
        EntryService entryService = new EntryServiceImpl(entryDao);
        EntryTimeService entryTimeService = new EntryTimeServiceImpl(entryDao);
        getLogger().info("Services initialized");

        getLogger().info("Starting monitors...");
        MainThreadSync mainThreadSync = new MainThreadSync(this);
        PlayerKicker playerKicker = new PlayerKicker(configsContainer.getMessages(), mainThreadSync);
        expireMonitor = new ExpireMonitor(entryDao, configsContainer.getSettings(), playerKicker);
        lastJoinMonitor = new LastJoinMonitor(entryDao, configsContainer.getSettings());
        notifyMonitor = new NotifyMonitor(entryDao, configsContainer, new MessageSender());
        getLogger().info("Monitors started");

        getLogger().info("Loading time convertor");
        TimeUnitsConvertorSettings timeUnitsConvertorSettings = configsContainer.getTimeUnitsConvertorSettings();
        timeConvertor = new TimeConvertor(timeUnitsConvertorSettings);
        TimeRandom timeRandom = new TimeRandom(timeConvertor);
        getLogger().info("Time convertor loaded");

        getLogger().info("Loading player login filter...");
        UnfreezeEntryChecker unfreezeEntryChecker = new UnfreezeEntryCheckerImpl(configsContainer.getSettings().isUnfreezeTimeOnPlayerJoin(), entryService);
        AccessEntryChecker accessEntryChecker = new AccessEntryCheckerImpl();
        playerLoginFilter = new PlayerLoginFilter(configsContainer.getMessages(), entryFinder,
                unfreezeEntryChecker, accessEntryChecker);
        getServer().getPluginManager().registerEvents(playerLoginFilter, this);
        getLogger().info("Player login filter loaded");

        getLogger().info("Loading commands...");
        CommandsLoader commandsLoader = new CommandsLoader(this,
                configsContainer.getCommandsConfig(), configsContainer.getMessages(),
                entryFinder, timeConvertor,
                entryService, timeRandom, entryTimeService);
        CommandDispatcher commandDispatcher = new CommandDispatcher(configsContainer.getMessages(), commandsLoader.load());
        CommandProxy commandProxy = new CommandProxy(configsContainer.getMessages(), commandDispatcher);

        WHITELIST_COMMANDS.forEach(commandName -> tryToBindCommandHandler(commandName, commandProxy));
        getLogger().info("Commands loaded");

        tryToLoadMetrics();
        tryToLoadPapi();

        getLogger().info("Plugin enabled");
    }

    private void tryToBindCommandHandler(String commandName, CommandProxy commandProxy) {
        try {
            PluginCommand command = getCommand(commandName);
            if (command == null) {
                getLogger().log(Level.WARNING, "Plugin command by name " + commandName + " not found (null)");
                return;
            }
            command.setTabCompleter(commandProxy);
            command.setExecutor(commandProxy);
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "Failed to register command handler " + commandName, exception);
        }
    }

    private void tryToLoadPapi() {
        try {
            if (configsContainer.getPlaceholders().isPlaceholdersEnabled()) {
                PlaceholderParser placeholderParser = new PlaceholderParserImpl(entryFinder, timeConvertor, configsContainer.getPlaceholders());
                PlaceholderHookProxy hook = new PlaceholderHookProxy(placeholderParser, version);
                hook.register();
                getLogger().info("PAPI hooked");
            }
        } catch (Exception exception) {
            getLogger().log(Level.WARNING, "Failed to load PAPI", exception);
        }
    }

    private void tryToLoadMetrics() {
        try {
            MetricsUtils.tryToLoad(this);
            getLogger().info("Metrics loaded");
        } catch (Exception exception) {
            getLogger().log(Level.WARNING, "Failed to load metrics", exception);
        }
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
        getLogger().info("Disabling plugin...");
        if (playerLoginFilter != null) {
            playerLoginFilter.unregister();
        }
        if (entryDao != null) {
            entryDao.close();
        }
        if (expireMonitor != null) {
            expireMonitor.shutdown();
        }
        if (lastJoinMonitor != null) {
            lastJoinMonitor.shutdown();
        }
        if (notifyMonitor != null) {
            notifyMonitor.shutdown();
        }
        getLogger().info("Plugin disabled");
    }

    @Override
    public void reload() {
        tryToDisable();
        tryToEnable();
    }
}
