package ru.nightmirror.wlbytime.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.nightmirror.wlbytime.api.events.PlayerAddedToWhitelistEvent;
import ru.nightmirror.wlbytime.api.events.PlayerRemovedFromWhitelistEvent;
import ru.nightmirror.wlbytime.util.Util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Database {

    private static Database instance;
    private Boolean mySQLEnabled;
    private String conStr;
    private String user;
    private String password;
    private WhitelistByTime plugin;
    private final Logger log = Logger.getLogger("WhitelistByTime");

    public static Database getInstance() {
        if (instance == null) instance = new Database();
        return instance;
    }

    private Connection getConnection() {
        try {
            return mySQLEnabled ? DriverManager.getConnection(conStr, user, password) : DriverManager.getConnection(conStr);
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
        return null;
    }

    public void init(WhitelistByTime plugin) {
        Config config = Config.getInstance();

        this.plugin = plugin;

        if (config.getLine("is-mysql-enabled").equalsIgnoreCase("true")) {
            mySQLEnabled = true;
            conStr = "jdbc:mysql://" + config.getLine("mysql-connection");
            user = config.getLine("mysql-user");
            password = config.getLine("mysql-password");
        } else {
            mySQLEnabled = false;
            conStr = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + config.getLine("database-file-name");

            try {
                File directory = new File(plugin.getDataFolder().getAbsolutePath());

                if (!directory.exists()) {
                    directory.mkdirs();
                    new File(plugin.getDataFolder().getAbsolutePath() + File.separator + config.getLine("database-file-name")).createNewFile();
                }
            } catch (Exception e) {
                log.severe(e.getMessage());
            }
        }

        final String query = "CREATE TABLE IF NOT EXISTS whitelist (\n"
                + " `nickname` TEXT,\n"
                + " `until` INTEGER\n"
                + ");";

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
        ) {
            statement.execute(query);
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    public void addPlayer(String nickname, long until) {
        PlayerAddedToWhitelistEvent event = new PlayerAddedToWhitelistEvent(nickname, until);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        final String query = "INSERT INTO whitelist (`nickname`, `until`)" +
                "VALUES ('"+nickname+"', '"+until+"');";

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);

            if (until == -1L) {
                log.info(Config.getInstance().getLine("minecraft-commands.successfully-added")
                        .replaceAll("%player%", nickname));
            } else {
                log.info(Config.getInstance().getLine("minecraft-commands.successfully-added-time")
                        .replaceAll("%player%", nickname)
                        .replaceAll("%time%", Util.getTimeLine(until - System.currentTimeMillis())));
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    private Boolean checkPlayerInWhitelist(String nickname) {
        final String query = "SELECT * FROM whitelist WHERE nickname = '"+nickname+"';";

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
        ) {
            if (resultSet.next()) return true;
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
        return false;
    }

    public Boolean checkPlayer(String nickname) {
        Boolean inWhitelist = checkPlayerInWhitelist(nickname);

        if (inWhitelist) {
            long until = getUntil(nickname);

            if (until == -1L || until > System.currentTimeMillis()) {
                inWhitelist = true;
            } else {
                removePlayer(nickname);
                inWhitelist = false;
            }
        }

        if (!inWhitelist) {
            Player player = plugin.getServer().getPlayer(nickname);
            if (player != null && player.isOnline()) {
                player.kickPlayer(Config.getInstance().getLine("minecraft-commands.you-not-in-whitelist"));
            }
        }

        return inWhitelist;
    }

    public long getUntil(String nickname) {
        final String query = "SELECT * FROM whitelist WHERE nickname = '"+nickname+"';";

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
        ) {
            if (resultSet.next()) {
                long until = resultSet.getLong("until");
                return until;
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
        return -1L;
    }

    public void removePlayer(String nickname) {
        PlayerRemovedFromWhitelistEvent event = new PlayerRemovedFromWhitelistEvent(nickname);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        final String query = "DELETE FROM whitelist WHERE nickname = '"+nickname+"';";

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);

            log.info(Config.getInstance().getLine("minecraft-commands.player-removed-from-whitelist")
                    .replaceAll("%player%", nickname));
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    public List<String> getAll() {
        final String query = "SELECT * FROM whitelist;";

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
        ) {
            List<String> nicknames = new ArrayList<>();

            while (resultSet.next()) {
                String nickname = resultSet.getString("nickname");

                if (checkPlayer(nickname)) {
                    nicknames.add(nickname);
                }
            }

            return nicknames;
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
        return null;
    }
}
