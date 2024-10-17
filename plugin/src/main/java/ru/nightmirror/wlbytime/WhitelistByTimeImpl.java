package ru.nightmirror.wlbytime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.wlbytime.common.command.WhitelistCommandExecutor;
import ru.nightmirror.wlbytime.common.command.WhitelistTabCompleterExecutor;
import ru.nightmirror.wlbytime.common.listeners.PlayerKicker;
import ru.nightmirror.wlbytime.common.listeners.PlayerLoginListener;
import ru.nightmirror.wlbytime.common.listeners.WhitelistCmdListener;
import ru.nightmirror.wlbytime.common.placeholder.PlaceholderHook;
import ru.nightmirror.wlbytime.common.utils.BukkitSyncer;
import ru.nightmirror.wlbytime.common.utils.MetricsUtils;
import ru.nightmirror.wlbytime.config.ConfigsContainer;
import ru.nightmirror.wlbytime.filters.InactivePlayerRemover;
import ru.nightmirror.wlbytime.impl.command.CommandsExecutorImpl;
import ru.nightmirror.wlbytime.impl.command.TabCompleterImpl;
import ru.nightmirror.wlbytime.impl.database.PlayerDaoImpl;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.models.DatabaseSettings;
import ru.nightmirror.wlbytime.predicates.ConnectingPlayersPredicate;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeUnitsConvertorSettings;

import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WhitelistByTimeImpl extends JavaPlugin implements WhitelistByTime {

    static Logger log;

    boolean whitelistEnabled = true;

    TimeConvertor timeConvertor;
    BukkitSyncer syncer;

    TimeUnitsConvertorSettings timeSettings;

    @Getter
    ConfigsContainer configs;

    PlayerDaoImpl database;
    PlaceholderHook placeholderHook;

    ScheduledExecutorService scheduler;

    @Override
    @SneakyThrows
    public void onEnable() {
        log = getLogger();

        scheduler = Executors.newSingleThreadScheduledExecutor();

        checkCompatibility();

        syncer = new BukkitSyncer(this);

        configs = new ConfigsContainer(getDataFolder());
        configs.load();

        whitelistEnabled = configs.getSettings().isEnabled();

        initTimeConvertor();

        try {
            initDatabase();
        } catch (SQLException exception) {
            getLogger().log(Level.SEVERE, "Can't init database", exception);
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
    @SneakyThrows
    public void onDisable() {
        HandlerList.unregisterAll(this);

        if (placeholderHook != null) placeholderHook.unregister();
        if (database != null) database.close();

        scheduler.shutdown();

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
        timeSettings = TimeUnitsConvertorSettings.builder()
                .year(getConfigs().getSettings().getTimeUnitsYear())
                .month(getConfigs().getSettings().getTimeUnitsMonth())
                .week(getConfigs().getSettings().getTimeUnitsWeek())
                .day(getConfigs().getSettings().getTimeUnitsDay())
                .hour(getConfigs().getSettings().getTimeUnitsHour())
                .minute(getConfigs().getSettings().getTimeUnitsMinute())
                .second(getConfigs().getSettings().getTimeUnitsSecond())
                .forever(getConfigs().getMessages().getForever())
                .build();

        timeConvertor = new TimeConvertor(timeSettings);
    }

    private void initDatabase() throws SQLException {
        DatabaseSettings settings = DatabaseSettings.builder()
                .localStorageDir(getDataFolder())
                .type(getConfigs().getDatabase().getType())
                .address(getConfigs().getDatabase().getAddress())
                .databaseName(getConfigs().getDatabase().getName())
                .userUserAndPassword(getConfigs().getDatabase().isUseUserAndPassword())
                .user(getConfigs().getDatabase().getUser())
                .password(getConfigs().getDatabase().getPassword())
                .params(getConfigs().getDatabase().getParams())
                .build();

        database = new PlayerDaoImpl(settings, configs.getSettings().isCaseSensitive());
        database.loadPlayersToCache(Arrays.stream(getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .toList());
    }

    private void initCommandsAndListeners() {
        getServer().getPluginManager().registerEvents(new WhitelistCmdListener(new CommandsExecutorImpl(database, this, timeConvertor)), this);
        Predicate<ConnectingPlayersPredicate.ConnectingPlayer> filter = new ConnectingPlayersPredicate(
                database,
                configs.getSettings().isCaseSensitive(),
                this
        );
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this, database, filter), this);

        getCommand("whitelist").setExecutor(new WhitelistCommandExecutor(new CommandsExecutorImpl(database, this, timeConvertor)));
        getCommand("whitelist").setTabCompleter(new WhitelistTabCompleterExecutor(new TabCompleterImpl(database, timeSettings, this)));
    }

    private void initChecker() {
        PlayerKicker playerKicker = new PlayerKicker(syncer, this, getConfigs().getSettings().isCaseSensitive(), getConfigs().getMessages().youNotInWhitelistKick);

        Runnable inactivePlayerRemover = new InactivePlayerRemover(database, playerKicker, Duration.of(getConfigs().getSettings().checkerDelay, ChronoUnit.MILLIS));
        long delayBetweenChecksInMs = getConfigs().getSettings().getCheckerDelay();
        scheduler.scheduleWithFixedDelay(inactivePlayerRemover, delayBetweenChecksInMs, delayBetweenChecksInMs, TimeUnit.MILLISECONDS);
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
        MetricsUtils.tryToLoad(this);
    }

    @Override
    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    @Override
    public void setWhitelistEnabled(boolean mode) {
        whitelistEnabled = mode;
    }

    @SuppressWarnings("unused")
    public static void info(String message) {
        if (log != null) log.info(message);
    }

    @SuppressWarnings("unused")
    public static void warn(String message) {
        if (log != null) log.warning(message);
    }

    @SuppressWarnings("unused")
    public static void error(String message) {
        if (log != null) log.severe(message);
    }
}
