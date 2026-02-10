package ru.nightmirror.wlbytime.identity;

import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FloodgateBridge {

    private final boolean available;
    private final Logger logger;

    private FloodgateBridge(boolean available, Logger logger) {
        this.available = available;
        this.logger = logger;
    }

    public static FloodgateBridge create(Logger logger) {
        try {
            FloodgateApi.getInstance();
            return new FloodgateBridge(true, logger);
        } catch (NoClassDefFoundError e) {
            logger.log(Level.INFO, "Floodgate API not found, skipping");
            return new FloodgateBridge(false, logger);
        }
    }

    public boolean isFloodgatePlayer(UUID uuid) {
        if (!available) {
            return false;
        }
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public Optional<UUID> getCorrectUniqueId(UUID uuid) {
        if (!available) {
            return Optional.empty();
        }
        try {
            FloodgatePlayer player = FloodgateApi.getInstance().getPlayer(uuid);
            if (player == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(player.getCorrectUniqueId());
        } catch (NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

    public Optional<String> getCorrectUsername(UUID uuid) {
        if (!available) {
            return Optional.empty();
        }
        try {
            FloodgatePlayer player = FloodgateApi.getInstance().getPlayer(uuid);
            if (player == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(player.getCorrectUsername());
        } catch (NoClassDefFoundError e) {
            return Optional.empty();
        }
    }
}
