package ru.nightmirror.wlbytime.common.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.interfaces.database.IDatabase;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.covertors.time.TimeConvertor;

public class PlaceholderHook extends PlaceholderExpansion {

    private final IDatabase database;
    private final FileConfiguration config;
    private final Plugin plugin;

    public PlaceholderHook(IDatabase database, Plugin plugin) {
        this.database = database;
        this.config = plugin.getConfig();
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "wlbytime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "whitelistbytime";
    }

    @Override
    public @NotNull String getVersion() {
        return "5.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("in_whitelist")) {
            String result = database.checkPlayer(player.getName()) ? config.getString("in-whitelist-true", "YES") : config.getString("in-whitelist-false", "NO");
            return ColorsConvertor.convert(result);
        } else if (params.equalsIgnoreCase("time_left")) {
            String time = TimeConvertor.getTimeLine(plugin, database.getUntil(player.getName()) - System.currentTimeMillis(), true);
            return ColorsConvertor.convert(config.getString("time-left", "&a%time%").replaceAll("%time%", time));
        }

        return "{ERR_PARAM}";
    }
}
