package ru.nightmirror.wlbytime;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bstats.bukkit.Metrics;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.wlbytime.common.checker.PlayersChecker;
import ru.nightmirror.wlbytime.common.command.CommandsExecutor;
import ru.nightmirror.wlbytime.common.command.WhitelistCommandExecutor;
import ru.nightmirror.wlbytime.common.command.WhitelistTabCompleter;
import ru.nightmirror.wlbytime.common.covertors.time.TimeConvertor;
import ru.nightmirror.wlbytime.common.covertors.time.TimeUnitsConvertorSettings;
import ru.nightmirror.wlbytime.common.database.WLDatabase;
import ru.nightmirror.wlbytime.common.database.misc.DatabaseSettings;
import ru.nightmirror.wlbytime.common.listeners.PlayerKicker;
import ru.nightmirror.wlbytime.common.listeners.PlayerLoginListener;
import ru.nightmirror.wlbytime.common.listeners.WhitelistCmdListener;
import ru.nightmirror.wlbytime.common.placeholder.PlaceholderHook;
import ru.nightmirror.wlbytime.common.utils.BukkitSyncer;
import ru.nightmirror.wlbytime.common.utils.ConfigUtils;
import ru.nightmirror.wlbytime.interfaces.IWhitelist;
import ru.nightmirror.wlbytime.interfaces.checker.Checker;

import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WhitelistByTime extends JavaPlugin implements IWhitelist {

    static Logger log;

    boolean whitelistEnabled = true;

    TimeConvertor timeConvertor;
    BukkitSyncer syncer;

    WLDatabase database;
    Checker checker;
    PlaceholderHook placeholderHook;
    Metrics metrics;

    @Override
    public void onEnable() {
        log = getLogger();
        syncer = new BukkitSyncer(this);

        ConfigUtils.checkConfig(this);
        whitelistEnabled = getConfig().getBoolean("enabled", true);

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
        if (metrics != null) metrics.shutdown();
        if (checker != null) checker.stop();
        if (database != null) {
            database.close().join();
        }

        if (getCommand("whitelist") != null) {
            getCommand("whitelist").setExecutor(null);
            getCommand("whitelist").setTabCompleter(null);
        }

        info("Disabled");
    }

    @Override
    public void reload() {
        onDisable();
        onEnable();
    }

    private void initTimeConvertor() {
        TimeUnitsConvertorSettings settings = TimeUnitsConvertorSettings.builder()
                .year(getConfig().getStringList("time-units.year"))
                .month(getConfig().getStringList("time-units.month"))
                .week(getConfig().getStringList("time-units.week"))
                .day(getConfig().getStringList("time-units.day"))
                .hour(getConfig().getStringList("time-units.hour"))
                .minute(getConfig().getStringList("time-units.minute"))
                .second(getConfig().getStringList("time-units.second"))
                .forever(getConfig().getString("minecraft-commands.forever", "forever"))
                .build();

        timeConvertor = new TimeConvertor(settings);
    }

    private void initDatabase() throws SQLException {
        DatabaseSettings settings = DatabaseSettings.builder()
                .localStorageDir(getDataFolder())
                .type(getConfig().getString("type", "sqlite"))
                .address(getConfig().getString("address", "localhost"))
                .databaseName(getConfig().getString("name", "whitelist"))
                .userUserAndPassword(getConfig().getBoolean("userUserAndPassword", false))
                .user(getConfig().getString("user", "user"))
                .password(getConfig().getString("password", "password"))
                .params(getConfig().getStringList("params"))
                .build();

        database = new WLDatabase(settings);
        database.loadPlayersToCache(Arrays.stream(getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .toList());
    }

    private void initCommandsAndListeners() {
        getServer().getPluginManager().registerEvents(new WhitelistCmdListener(new CommandsExecutor(database, this, timeConvertor)), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(database, getConfig().getBoolean("case-sensitive", false),this), this);

        getCommand("whitelist").setExecutor(new WhitelistCommandExecutor(new CommandsExecutor(database, this, timeConvertor)));
        getCommand("whitelist").setTabCompleter(new WhitelistTabCompleter(database, this));
    }

    private void initChecker() {
        PlayerKicker playerKicker = new PlayerKicker(syncer, this, getConfig().getBoolean("case-sensitive", false), getConfig().getStringList("minecraft-commands.you-not-in-whitelist-kick"));
        database.addListener(playerKicker);

        checker = new PlayersChecker(database, playerKicker, Duration.of(getConfig().getInt("checker-delay", 1000), ChronoUnit.MILLIS));
        checker.start();
    }

    private void hookPlaceholder() {
        if (getConfig().getBoolean("placeholders-enabled", false)) {
            try {
                placeholderHook = new PlaceholderHook(database, timeConvertor, getPluginConfig());
                placeholderHook.register();
                log.info("Hooked with PlaceholderAPI");
            } catch (Exception exception) {
                log.warning("Can't hook with PlaceholderAPI. " + exception.getMessage());
            }
        }
    }

    private void initMetrics() {
        metrics = new Metrics(this, 13834);
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
