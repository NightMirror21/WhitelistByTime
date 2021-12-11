package ru.nightmirror.wlbytime.main;

import org.bukkit.ChatColor;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SQLite {

    private static SQLite instance;
    private String con_str;
    private WhitelistByTime plugin;
    private final Logger log = Logger.getLogger("WhitelistByTime");

    public static SQLite getInstance() {
        if (instance == null) instance = new SQLite();
        return instance;
    }

    public void init(WhitelistByTime plugin) {
        this.plugin = plugin;

        try {
            File directory = new File(plugin.getDataFolder().getAbsolutePath());

            if (!directory.exists()) {
                directory.mkdirs();
                new File(plugin.getDataFolder().getAbsolutePath() + "/data.db").createNewFile();
            }

            con_str = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/data.db";

            try (
                    Connection connection = DriverManager.getConnection(con_str);
                    Statement statement = connection.createStatement();
            ) {
                String sql = "CREATE TABLE IF NOT EXISTS whitelist (\n"
                        + " `nickname` TEXT,\n"
                        + " `until` INTEGER\n"
                        + ");";
                statement.execute(sql);
            }

        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    public void addPlayer(String nickname, long until) {
        try (
                Connection connection = DriverManager.getConnection(con_str);
                Statement statement = connection.createStatement();
        ) {

            final String query = "INSERT INTO whitelist (`nickname`, `until`)" +
                    "VALUES ('"+nickname+"', '"+until+"');";

            statement.executeUpdate(query);

            if (until == -1L) {
                log.info(ChatColor.GREEN + "Player " + nickname + " added to whitelist forever");
            } else {
                log.info(ChatColor.GREEN + "Player " + nickname + " added to whitelist for " + Util.getTimeLine(until - System.currentTimeMillis()));
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    private Boolean checkPlayerInWhitelist(String nickname) {
        try (
                Connection connection = DriverManager.getConnection(con_str);
                Statement statement = connection.createStatement();
        ) {
            final String query = "SELECT * FROM whitelist WHERE nickname = '"+nickname+"';";

            final ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                return true;
            }
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

        return inWhitelist;
    }

    public long getUntil(String nickname) {
        try (
                Connection connection = DriverManager.getConnection(con_str);
                Statement statement = connection.createStatement();
        ) {
            final String query = "SELECT * FROM whitelist WHERE nickname = '"+nickname+"';";

            final ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                long until = resultSet.getLong("until");

                return until;
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
        return -1L;
    }

    public void removePlayer(String nickname) {
        try (
                Connection connection = DriverManager.getConnection(con_str);
                Statement statement = connection.createStatement();
        ) {
            final String query = "DELETE FROM whitelist WHERE nickname = '"+nickname+"';";

            statement.executeUpdate(query);

            log.info(ChatColor.YELLOW + "Player " + nickname + " removed from whitelist");
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    public List<String> getAll() {
        try (
                Connection connection = DriverManager.getConnection(con_str);
                Statement statement = connection.createStatement();
        ) {
            final String query = "SELECT * FROM whitelist;";

            final ResultSet resultSet = statement.executeQuery(query);

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
