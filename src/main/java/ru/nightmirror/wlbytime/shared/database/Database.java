package ru.nightmirror.wlbytime.shared.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.nightmirror.wlbytime.interfaces.database.IDatabase;
import ru.nightmirror.wlbytime.misc.convertors.ColorsConvertor;
import ru.nightmirror.wlbytime.misc.convertors.TimeConvertor;
import ru.nightmirror.wlbytime.shared.api.events.PlayerAddedToWhitelistEvent;
import ru.nightmirror.wlbytime.shared.api.events.PlayerRemovedFromWhitelistEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Database implements IDatabase {
    private Boolean useUserAndPassword;
    private String conStr;
    private final Plugin plugin;
    private final static Logger LOG = Logger.getLogger("WhitelistByTime");

    public Database(Plugin plugin) {
        this.plugin = plugin;
        enable();
    }

    private void enable() {
        conStr = getStringSource();
        useUserAndPassword = getConfigBoolean("use-user-and-password", false);

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
                Statement statement = connection.createStatement()
        ) {
            statement.execute(query);
        } catch (Exception exception) {
            LOG.severe("Can't create table: " + exception.getMessage());
        }
    }

    private String getStringSource() {
        final String type = getConfigString("type", "sqlite");
        if (type.equalsIgnoreCase("sqlite") || type.equalsIgnoreCase("h2"))
            return "jdbc:" + type + ":" + new File(plugin.getDataFolder(), "database.db").getAbsolutePath();

        return "jdbc:" + type + "://" + getConfigString("address") + File.separator + getConfigString("name");
    }

    private Connection getConnection() {
        try {
            return useUserAndPassword ? DriverManager.getConnection(conStr, getConfigString("user"), getConfigString("password")) : DriverManager.getConnection(conStr);
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
                Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);

            if (until == -1L) {
                LOG.info("Player "+nickname+" added to whitelist forever");
            } else {
                LOG.info("Player "+nickname+" added to whitelist for "+TimeConvertor.getTimeLine(plugin, until-System.currentTimeMillis()));
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
                ResultSet resultSet = statement.executeQuery(query)
        ) {
            if (resultSet.next()) return true;
        } catch (Exception exception) {
            LOG.warning("Can't check player: " + exception.getMessage());
        }
        return false;
    }

    @Override
    public Boolean checkPlayer(String nickname) {
        boolean inWhitelist = checkPlayerInWhitelist(nickname);

        if (inWhitelist) {
            long until = getUntil(nickname);

            if (until != -1L && until < System.currentTimeMillis()) {
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

    private Boolean checkPlayer(String nickname, Connection connection) {
        boolean inWhitelist = checkPlayerInWhitelist(nickname);

        if (inWhitelist) {
            long until = getUntil(nickname);

            if (until == -1L || until > System.currentTimeMillis()) {
                inWhitelist = true;
            } else {
                removePlayer(nickname, connection);
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
                ResultSet resultSet = statement.executeQuery(query)
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
    public void setUntil(String nickname, long until) {
        final String query = "UPDATE whitelist SET until = '"+until+"' WHERE nickname = '"+nickname+"';";

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement()
                ) {
            statement.executeUpdate(query);

            if (until == -1L) {
                LOG.info("Set time for "+nickname+" forever");
            } else {
                LOG.info("Set time for "+nickname+" "+TimeConvertor.getTimeLine(plugin, until-System.currentTimeMillis()));
            }
        } catch (Exception exception) {
            LOG.warning("Can't set time for player: " + nickname);
        }
    }

    @Override
    public void removePlayer(String nickname) {
        PlayerRemovedFromWhitelistEvent event = new PlayerRemovedFromWhitelistEvent(nickname);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        final String query = "DELETE FROM whitelist WHERE nickname = '"+nickname+"';";
        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);

            LOG.info("Player "+nickname+" removed");
        } catch (Exception exception) {
            LOG.warning("Can't remove player: " + exception.getMessage());
        }
    }

    private void removePlayer(String nickname, Connection connection) {
        PlayerRemovedFromWhitelistEvent event = new PlayerRemovedFromWhitelistEvent(nickname);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        final String query = "DELETE FROM whitelist WHERE nickname = '"+nickname+"';";
        try (
                Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);

            LOG.info("Player "+nickname+" removed");
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
                ResultSet resultSet = statement.executeQuery(query)
        ) {
            List<String> nicknames = new ArrayList<>();

            while (resultSet.next()) {
                String nickname = resultSet.getString("nickname");

                if (checkPlayer(nickname, connection)) {
                    nicknames.add(nickname);
                }
            }

            return nicknames;
        } catch (Exception exception) {
            LOG.warning("Can't get all players: " + exception.getMessage());
        }
        return null;
    }

    private String getConfigString(String path) {
        return getConfigString(path, "null");
    }

    private String getConfigString(String path, String def) {
        return plugin.getConfig().getString(path, def);
    }

    private Boolean getConfigBoolean(String path) {
        return getConfigBoolean(path, false);
    }

    private Boolean getConfigBoolean(String path, Boolean def) {
        return plugin.getConfig().getBoolean(path, def);
    }
}
