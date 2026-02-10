package ru.nightmirror.wlbytime.identity;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import lombok.extern.slf4j.Slf4j;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.identity.PlayerIdMode;
import ru.nightmirror.wlbytime.identity.PlayerKey;
import ru.nightmirror.wlbytime.identity.ResolvedPlayer;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.identity.PlayerIdentityResolver;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
public class PlayerIdentityResolverImpl implements PlayerIdentityResolver {

    private final SettingsConfig settings;
    private final Server server;
    private final MojangApiClient mojangApiClient;
    private final FloodgateBridge floodgate;

    public PlayerIdentityResolverImpl(SettingsConfig settings, Server server, Logger logger) {
        this.settings = settings;
        this.server = server;
        this.mojangApiClient = new MojangApiClient(
                settings.getMojangTimeoutMs(),
                settings.isMojangCacheEnabled(),
                settings.getMojangCacheTtlMs()
        );
        this.floodgate = FloodgateBridge.create(logger);
    }

    @Override
    public @NotNull ResolvedPlayer resolveByNickname(@NotNull String nickname) {
        PlayerIdMode mode = resolveMode();
        return switch (mode) {
            case OFFLINE -> resolvedNickname(nickname);
            case ONLINE -> resolveOnlineNickname(nickname);
            case FLOODGATE -> resolveFloodgateNickname(nickname);
            case AUTO -> server.getOnlineMode() ? resolveOnlineNickname(nickname) : resolvedNickname(nickname);
        };
    }

    @Override
    public @NotNull ResolvedPlayer resolveByIssuer(@NotNull CommandIssuer issuer) {
        PlayerIdMode mode = resolveMode();
        if (mode == PlayerIdMode.OFFLINE) {
            return resolvedNickname(issuer.getNickname());
        }

        Optional<UUID> issuerUuid = issuer.getUuid();
        if (issuerUuid.isEmpty()) {
            return resolvedNickname(issuer.getNickname());
        }

        UUID uuid = issuerUuid.get();
        if (mode == PlayerIdMode.FLOODGATE) {
            UUID floodgateUuid = resolveFloodgateUuid(uuid);
            if (floodgateUuid != null) {
                return resolvedUuid(issuer.getNickname(), floodgateUuid);
            }
            return resolvedNickname(issuer.getNickname());
        }

        return resolvedUuid(issuer.getNickname(), uuid);
    }

    @Override
    public @NotNull ResolvedPlayer resolveByLogin(@NotNull String nickname, @NotNull UUID uuid) {
        PlayerIdMode mode = resolveMode();
        return switch (mode) {
            case OFFLINE -> resolvedNickname(nickname);
            case ONLINE -> resolvedUuid(nickname, uuid);
            case FLOODGATE -> {
                UUID floodgateUuid = resolveFloodgateUuid(uuid);
                if (floodgateUuid != null) {
                    yield resolvedUuid(nickname, floodgateUuid);
                }
                yield resolvedNickname(nickname);
            }
            case AUTO -> server.getOnlineMode() ? resolvedUuid(nickname, uuid) : resolvedNickname(nickname);
        };
    }

    private PlayerIdMode resolveMode() {
        PlayerIdMode mode = settings.getPlayerIdMode();
        return mode == null ? PlayerIdMode.OFFLINE : mode;
    }

    private ResolvedPlayer resolveOnlineNickname(String nickname) {
        Player player = server.getPlayerExact(nickname);
        if (player != null) {
            return resolvedUuid(nickname, player.getUniqueId());
        }

        OfflinePlayer offline = server.getOfflinePlayer(nickname);
        if (offline.hasPlayedBefore()) {
            return resolvedUuid(nickname, offline.getUniqueId());
        }

        if (settings.isMojangLookupEnabled()) {
            Optional<UUID> uuid = mojangApiClient.lookupUuid(nickname);
            if (uuid.isPresent()) {
                return resolvedUuid(nickname, uuid.get());
            }
            log.debug("Mojang lookup did not return UUID for nickname={}", nickname);
        } else {
            log.debug("Mojang lookup disabled, returning nickname-only identity for nickname={}", nickname);
        }

        return resolvedNickname(nickname);
    }

    private ResolvedPlayer resolveFloodgateNickname(String nickname) {
        if (floodgate == null) {
            log.debug("Floodgate bridge unavailable, falling back to online lookup for nickname={}", nickname);
            return resolveOnlineNickname(nickname);
        }

        for (Player player : server.getOnlinePlayers()) {
            if (!floodgate.isFloodgatePlayer(player.getUniqueId())) {
                continue;
            }
            Optional<String> correctName = floodgate.getCorrectUsername(player.getUniqueId());
            if (correctName.isPresent() && correctName.get().equalsIgnoreCase(nickname)) {
                UUID floodgateUuid = floodgate.getCorrectUniqueId(player.getUniqueId()).orElse(player.getUniqueId());
                return resolvedUuid(nickname, floodgateUuid);
            }
        }

        return resolveOnlineNickname(nickname);
    }

    private @Nullable UUID resolveFloodgateUuid(UUID uuid) {
        if (floodgate == null) {
            log.debug("Floodgate bridge unavailable, skipping floodgate UUID resolution");
            return null;
        }
        if (!floodgate.isFloodgatePlayer(uuid)) {
            log.debug("Player UUID={} is not a floodgate player", uuid);
            return null;
        }
        return floodgate.getCorrectUniqueId(uuid).orElse(uuid);
    }

    private ResolvedPlayer resolvedNickname(String nickname) {
        return new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null);
    }

    private ResolvedPlayer resolvedUuid(String nickname, UUID uuid) {
        return new ResolvedPlayer(PlayerKey.uuid(uuid), nickname, uuid);
    }
}
