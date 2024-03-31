package ru.nightmirror.wlbytime.common.placeholder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.common.config.configs.PlaceholdersConfig;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.covertors.time.TimeConvertor;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;
import ru.nightmirror.wlbytime.interfaces.misc.VersionGetter;

import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaceholderHook extends PlaceholderExpansion {

    VersionGetter versionGetter;
    PlayerAccessor playerAccessor;
    TimeConvertor timeConvertor;
    PlaceholdersConfig config;

    @Override
    public @NotNull String getIdentifier() {
        return WhitelistByTime.getPAPIIdentifier();
    }

    @Override
    public @NotNull String getAuthor() {
        return "WhitelistByTime";
    }

    @Override
    public @NotNull String getVersion() {
        return versionGetter.getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        if (params.equalsIgnoreCase("in_whitelist")) {
            String result = playerAccessor.getPlayerCached(player.getName()).isPresent() ? config.inWhitelistTrue : config.inWhitelistFalse;
            return ColorsConvertor.checkLegacy(result);
        } else if (params.equalsIgnoreCase("time_left")) {
            AtomicReference<String> time = new AtomicReference<>("");

            playerAccessor.getPlayerCached(player.getName()).ifPresent(whitelistedPlayer -> {
                time.set(timeConvertor.getTimeLine(whitelistedPlayer.getUntil() - System.currentTimeMillis()));
            });

            return ColorsConvertor.checkLegacy(config.timeLeft.replaceAll("%time%", time.get()));
        }

        return "{ERR_PARAM}";
    }
}
