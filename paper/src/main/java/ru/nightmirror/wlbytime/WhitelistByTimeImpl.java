package ru.nightmirror.wlbytime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.wlbytime.common.command.CommandsExecutorImpl;
import ru.nightmirror.wlbytime.common.command.WhitelistCommandExecutor;
import ru.nightmirror.wlbytime.common.command.WhitelistTabCompleter;
import ru.nightmirror.wlbytime.common.command.WhitelistTabCompleterExecutor;
import ru.nightmirror.wlbytime.common.config.ConfigsContainer;
import ru.nightmirror.wlbytime.common.covertors.time.TimeConvertor;
import ru.nightmirror.wlbytime.common.covertors.time.TimeUnitsConvertorSettings;
import ru.nightmirror.wlbytime.common.database.DatabaseImpl;
import ru.nightmirror.wlbytime.common.database.misc.DatabaseSettings;
import ru.nightmirror.wlbytime.common.filters.ConnectingPlayersFilter;
import ru.nightmirror.wlbytime.common.filters.OnlinePlayersFilter;
import ru.nightmirror.wlbytime.common.listeners.PlayerKicker;
import ru.nightmirror.wlbytime.common.listeners.PlayerLoginListener;
import ru.nightmirror.wlbytime.common.listeners.WhitelistCmdListener;
import ru.nightmirror.wlbytime.common.placeholder.PlaceholderHook;
import ru.nightmirror.wlbytime.common.utils.BukkitSyncer;
import ru.nightmirror.wlbytime.common.utils.MetricsLoader;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.checker.Switchable;

import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WhitelistByTimeImpl extends JavaPlugin implements WhitelistByTime {

    static Logger log;

    boolean whitelistEnabled = true;

    TimeConvertor timeConvertor;
    BukkitSyncer syncer;

    @Getter
    ConfigsContainer configs;

    DatabaseImpl database;
    Switchable onlinePlayersFilter;
    PlaceholderHook placeholderHook;

    @Override
    public void onEnable() {
        log = getLogger();

        checkCompatibility();

        syncer = new BukkitSyncer(this);

        configs = new ConfigsContainer(getDataFolder());
        configs.load();

        whitelistEnabled = configs.getSettings().enabled;

        initTimeConvertor();

        try {
            initDatabase();
        } catch (SQLException exception) {
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        initChecker();
        initCommandsAndListeners();
        initMetrics();
        hookPlaceholder();

        info("Enabled");
    }

    @Override
    public String getVersion() {
        try {
            return getDescription().getVersion();
        } catch (NoSuchMethodError error) {
            return getPluginMeta().getDescription();
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        if (placeholderHook != null) placeholderHook.unregister();
        if (onlinePlayersFilter != null) onlinePlayersFilter.stop();
        if (database != null) database.close();

        info("Disabled");
    }

    @Override
    public void reload() {
        onDisable();
        onEnable();
    }

    private void checkCompatibility() {
        String core = Bukkit.getServer().getName();

        if (core.equalsIgnoreCase("spigot")) {
            log.warning("Hey! You running WhitelistByTime for Paper on Spigot - it is not cool!");
            log.warning("For better performance, we recommend downloading the Spigot version of plugin");
        }
    }

    private void initTimeConvertor() {
        TimeUnitsConvertorSettings settings = TimeUnitsConvertorSettings.builder()
                .year(getConfigs().getSettings().timeUnitsYear)
                .month(getConfigs().getSettings().timeUnitsMonth)
                .week(getConfigs().getSettings().timeUnitsWeek)
                .day(getConfigs().getSettings().timeUnitsDay)
                .hour(getConfigs().getSettings().timeUnitsHour)
                .minute(getConfigs().getSettings().timeUnitsMinute)
                .second(getConfigs().getSettings().timeUnitsSecond)
                .forever(getConfigs().getMessages().forever)
                .build();

        timeConvertor = new TimeConvertor(settings);
    }

    private void initDatabase() throws SQLException {
        DatabaseSettings settings = DatabaseSettings.builder()
                .localStorageDir(getDataFolder())
                .type(getConfigs().getDatabase().type)
                .address(getConfigs().getDatabase().address)
                .databaseName(getConfigs().getDatabase().name)
                .userUserAndPassword(getConfigs().getDatabase().useUserAndPassword)
                .user(getConfigs().getDatabase().user)
                .password(getConfigs().getDatabase().password)
                .params(getConfigs().getDatabase().params)
                .build();
        
        database = new DatabaseImpl(settings, configs.getSettings().caseSensitive);
        database.loadPlayersToCache(Arrays.stream(getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .toList());
    }

    private void initCommandsAndListeners() {
        getServer().getPluginManager().registerEvents(new WhitelistCmdListener(new CommandsExecutorImpl(database, this, timeConvertor)), this);
        Predicate<ConnectingPlayersFilter.ConnectingPlayer> filter = new ConnectingPlayersFilter(
                database,
                configs.getSettings().caseSensitive,
                this
        );
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this, database, filter), this);

        getCommand("whitelist").setExecutor(new WhitelistCommandExecutor(new CommandsExecutorImpl(database, this, timeConvertor)));
        getCommand("whitelist").setTabCompleter(new WhitelistTabCompleterExecutor(new WhitelistTabCompleter(database, this)));
    }

    private void initChecker() {
        PlayerKicker playerKicker = new PlayerKicker(syncer, this, getConfigs().getSettings().caseSensitive, getConfigs().getMessages().youNotInWhitelistKick);
        database.addListener(playerKicker);

        onlinePlayersFilter = new OnlinePlayersFilter(database, playerKicker, Duration.of(getConfigs().getSettings().checkerDelay, ChronoUnit.MILLIS));
        onlinePlayersFilter.start();
    }

    private void hookPlaceholder() {
        if (getConfigs().getPlaceholders().placeholdersEnabled) {
            try {
                placeholderHook = new PlaceholderHook(this, database, timeConvertor, configs.getPlaceholders());
                placeholderHook.register();
                log.info("Hooked with PlaceholderAPI");
            } catch (Exception exception) {
                log.warning("Can't hook with PlaceholderAPI. " + exception.getMessage());
            }
        }
    }

    private void initMetrics() {
        try {
            new MetricsLoader(this);
        } catch (Exception exception) {
            info("Failed to start collecting metrics. The plugin will continue working, but metrics will not be collected.");
        }
    }

    @Override
    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    @Override
    public void setWhitelistEnabled(boolean mode) {
        whitelistEnabled = mode;
    }

    public static void info(String message) {
        if (log != null) log.info(message);
    }

    public static void warn(String message) {
        if (log != null) log.warning(message);
    }

    public static void error(String message) {
        if (log != null) log.severe(message);
    }
}