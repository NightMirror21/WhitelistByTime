package ru.nightmirror.wlbytime.common.placeholder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.covertors.time.TimeConvertor;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaceholderHook extends PlaceholderExpansion {

    PlayerAccessor playerAccessor;
    TimeConvertor timeConvertor;
    FileConfiguration config;

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
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        if (params.equalsIgnoreCase("in_whitelist")) {
            String result = playerAccessor.getPlayerCached(player.getName()).isPresent() ? config.getString("in-whitelist-true", "YES") : config.getString("in-whitelist-false", "NO");
            return ColorsConvertor.convertHexAndLegacy(result);
        } else if (params.equalsIgnoreCase("time_left")) {
            AtomicReference<String> time = new AtomicReference<>("");

            playerAccessor.getPlayerCached(player.getName()).ifPresent(whitelistedPlayer -> {
                time.set(timeConvertor.getTimeLine(whitelistedPlayer.getUntil() - System.currentTimeMillis()));
            });

            return ColorsConvertor.convertHexAndLegacy(config.getString("time-left", "&a%time%").replaceAll("%time%", time.get()));
        }

        return "{ERR_PARAM}";
    }
}
