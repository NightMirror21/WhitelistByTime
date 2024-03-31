package ru.nightmirror.wlbytime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.wlbytime.common.checker.PlayersChecker;
import ru.nightmirror.wlbytime.common.command.CommandsExecutorImpl;
import ru.nightmirror.wlbytime.common.command.WhitelistTabCompleter;
import ru.nightmirror.wlbytime.common.config.ConfigsContainer;
import ru.nightmirror.wlbytime.common.covertors.time.TimeConvertor;
import ru.nightmirror.wlbytime.common.covertors.time.TimeUnitsConvertorSettings;
import ru.nightmirror.wlbytime.common.database.DatabaseImpl;
import ru.nightmirror.wlbytime.common.database.misc.DatabaseSettings;
import ru.nightmirror.wlbytime.common.listeners.PlayerKicker;
import ru.nightmirror.wlbytime.common.listeners.PlayerLoginListener;
import ru.nightmirror.wlbytime.common.listeners.WhitelistCmdListener;
import ru.nightmirror.wlbytime.common.listeners.command.WhitelistCommandExecutor;
import ru.nightmirror.wlbytime.common.listeners.command.WhitelistTabCompleterExecutor;
import ru.nightmirror.wlbytime.common.placeholder.PlaceholderHook;
import ru.nightmirror.wlbytime.common.utils.MetricsLoader;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.checker.Checker;

import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WhitelistByTimeImpl extends JavaPlugin implements WhitelistByTime {

    static Logger log;

    boolean whitelistEnabled = true;

    TimeConvertor timeConvertor;

    @Getter
    ConfigsContainer configs;

    DatabaseImpl database;
    Checker checker;
    PlaceholderHook placeholderHook;

    public static void info(String message) {
        if (log != null) log.info(message);
    }

    public static void warn(String message) {
        if (log != null) log.warning(message);
    }

    public static void error(String message) {
        if (log != null) log.severe(message);
    }

    @Override
    public void onEnable() {
        log = getLogger();

        whitelistEnabled = getConfigs().getSettings().enabled;

        configs = new ConfigsContainer(getDataFolder());
        configs.load();

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
    public void onDisable() {
        HandlerList.unregisterAll(this);

        if (placeholderHook != null) placeholderHook.unregister();
        if (checker != null) checker.stop();
        if (database != null) database.close();

        info("Disabled");
    }

    @Override
    public void reload() {
        onDisable();
        onEnable();
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

        database = new DatabaseImpl(settings);
        database.loadPlayersToCache(Arrays.stream(getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .toList());
    }

    private void initCommandsAndListeners() {
        getServer().getPluginManager().registerEvents(new WhitelistCmdListener(new CommandsExecutorImpl(database, this, timeConvertor)), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(database, getConfigs().getSettings().caseSensitive, this), this);

        getCommand("whitelist").setExecutor(new WhitelistCommandExecutor(new CommandsExecutorImpl(database, this, timeConvertor)));
        getCommand("whitelist").setTabCompleter(new WhitelistTabCompleterExecutor(new WhitelistTabCompleter(database, this)));
    }

    private void initChecker() {
        PlayerKicker playerKicker = new PlayerKicker(this, getConfigs().getSettings().caseSensitive, getConfigs().getMessages().youNotInWhitelistKick);
        database.addListener(playerKicker);

        checker = new PlayersChecker(database, playerKicker, Duration.of(getConfigs().getSettings().checkerDelay, ChronoUnit.MILLIS));
        checker.start();
    }

    private void hookPlaceholder() {
        if (getConfigs().getPlaceholders().placeholdersEnabled) {
            try {
                placeholderHook = new PlaceholderHook(database, timeConvertor, configs.getPlaceholders());
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
}
