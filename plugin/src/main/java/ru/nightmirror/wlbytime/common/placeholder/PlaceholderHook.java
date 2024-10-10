package ru.nightmirror.wlbytime.common.placeholder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.config.configs.PlaceholdersConfig;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.database.PlayerDao;
import ru.nightmirror.wlbytime.interfaces.misc.VersionGetter;
import ru.nightmirror.wlbytime.time.TimeConvertor;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaceholderHook extends PlaceholderExpansion {

    VersionGetter versionGetter;
    PlayerDao playerDao;
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
            String output =  playerDao.getPlayerCached(player.getName())
                    .map(d -> d.isFrozen() ? config.getFrozen() : config.getInWhitelistTrue())
                    .orElse(config.getInWhitelistFalse());

            return ColorsConvertor.checkLegacy(output);
        } else if (params.equalsIgnoreCase("time_left")) {
            String output = playerDao.getPlayerCached(player.getName())
                    .map(whitelistedPlayer -> timeConvertor.getTimeLine(whitelistedPlayer.calculateUntil() - System.currentTimeMillis()))
                    .orElse("");

            return ColorsConvertor.checkLegacy(config.getTimeLeft().replaceAll("%time%", output));
        }

        return "{ERR_PARAM}";
    }
}
