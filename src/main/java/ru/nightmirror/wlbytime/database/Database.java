package ru.nightmirror.wlbytime.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.nightmirror.wlbytime.api.events.PlayerAddedToWhitelistEvent;
import ru.nightmirror.wlbytime.api.events.PlayerRemovedFromWhitelistEvent;
import ru.nightmirror.wlbytime.convertors.ColorsConvertor;
import ru.nightmirror.wlbytime.convertors.TimeConvertor;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Database implements IDatabase {
    private Boolean mySQLEnabled;
    private String conStr;
    private final Plugin plugin;
    private final static Logger LOG = Logger.getLogger("WhitelistByTime");

    public Database(Plugin plugin) {
        this.plugin = plugin;
        enable();
    }

    private void enable() {
        if (plugin.getConfig().getBoolean("is-mysql-enabled", false)) {
            mySQLEnabled = true;
            conStr = "jdbc:mysql://" + plugin.getConfig().getString("mysql-connection");
        } else {
            mySQLEnabled = false;
            conStr = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + plugin.getConfig().getString("database-file-name", "database.dat");

            try {
                File directory = new File(plugin.getDataFolder().getAbsolutePath());

                if (!directory.exists()) {
                    directory.mkdirs();
                    new File(plugin.getDataFolder().getAbsolutePath() + File.separator + plugin.getConfig().getString("database-file-name", "database.dat")).createNewFile();
                }
            } catch (Exception e) {
                LOG.severe(e.getMessage());
            }
        }

        createTable();
    }

    @Override
    public void reload() {
        LOG.info("Reloading database...");
        enable();
    }

    private void createTable() {
        final String query = "CREATE TABLE IF NOT EXISTS whitelist (\n"
                + " `nickname` TEXT,\n"
                + " `until` INTEGER\n"
                + ");";

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
        ) {
            statement.execute(query);
        } catch (Exception exception) {
            LOG.severe("Can't create table: " + exception.getMessage());
        }
    }

    private Connection getConnection() {
        try {
            return mySQLEnabled ? DriverManager.getConnection(conStr, plugin.getConfig().getString("mysql-user"), plugin.getConfig().getString("mysql-password")) : DriverManager.getConnection(conStr);
        } catch (Exception exception) {
            LOG.severe("Can't create connection: " + exception.getMessage());
        }
        return null;
    }

    @Override
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
                LOG.info(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.successfully-added", "null"))
                        .replaceAll("%player%", nickname));
            } else {
                LOG.info(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.successfully-added-time", "null"))
                        .replaceAll("%player%", nickname)
                        .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until - System.currentTimeMillis())));
            }
        } catch (Exception exception) {
            LOG.warning("Can't add player: " + exception.getMessage());
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
        } catch (Exception exception) {
            LOG.warning("Can't check player: " + exception.getMessage());
        }
        return false;
    }

    @Override
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
                player.kickPlayer(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.you-not-in-whitelist", "null")));
            }
        }

        return inWhitelist;
    }

    @Override
    public long getUntil(String nickname) {
        final String query = "SELECT * FROM whitelist WHERE nickname = '"+nickname+"';";

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
        ) {
            if (resultSet.next()) {
                return resultSet.getLong("until");
            }
        } catch (Exception exception) {
            LOG.warning("Can't get until: " + exception.getMessage());
        }
        return -1L;
    }

    @Override
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

            LOG.info(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.you-not-in-whitelist", "null"))
                    .replaceAll("%player%", nickname));
        } catch (Exception exception) {
            LOG.warning("Can't remove player: " + exception.getMessage());
        }
    }

    @Override
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
        } catch (Exception exception) {
            LOG.warning("Can't get all players: " + exception.getMessage());
        }
        return null;
    }
}
