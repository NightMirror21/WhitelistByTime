package ru.nightmirror.wlbytime.config.configs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.LinkedHashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class CommandsConfig extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .build();

    @Comment(value = {
            @CommentValue("Permissions for whitelist subcommands")
    }, at = Comment.At.PREPEND)
    Set<String> addPermission = Set.of("whitelistbytime.add", "wlbytime.add");
    Set<String> checkPermission = Set.of("whitelistbytime.check", "wlbytime.check");
    Set<String> checkMePermission = Set.of("whitelistbytime.checkme", "wlbytime.checkme");
    Set<String> freezePermission = Set.of("whitelistbytime.freeze", "wlbytime.freeze");
    Set<String> unfreezePermission = Set.of("whitelistbytime.unfreeze", "wlbytime.unfreeze");
    Set<String> getAllPermission = Set.of("whitelistbytime.getall", "wlbytime.getall");
    Set<String> reloadPermission = Set.of("whitelistbytime.reload", "wlbytime.reload");
    Set<String> togglePermission = Set.of("whitelistbytime.toggle", "wlbytime.toggle");
    Set<String> removePermission = Set.of("whitelistbytime.remove", "wlbytime.remove");
    Set<String> timePermission = Set.of("whitelistbytime.time", "wlbytime.time");

    public CommandsConfig() {
        super(CommandsConfig.CONFIG);
    }

    public static CommandsConfig fromLegacy(LegacyCommandsConfig legacyConfig) {
        CommandsConfig config = new CommandsConfig();

        config.setAddPermission(toPermissions(legacyConfig.getAddPermission(), config.getAddPermission()));
        config.setCheckPermission(toPermissions(legacyConfig.getCheckPermission(), config.getCheckPermission()));
        config.setCheckMePermission(toPermissions(legacyConfig.getCheckMePermission(), config.getCheckMePermission()));
        config.setFreezePermission(toPermissions(legacyConfig.getFreezePermission(), config.getFreezePermission()));
        config.setUnfreezePermission(toPermissions(legacyConfig.getUnfreezePermission(), config.getUnfreezePermission()));
        config.setGetAllPermission(toPermissions(legacyConfig.getGetAllPermission(), config.getGetAllPermission()));
        config.setReloadPermission(toPermissions(legacyConfig.getReloadPermission(), config.getReloadPermission()));
        config.setTogglePermission(toPermissions(legacyConfig.getTogglePermission(), config.getTogglePermission()));
        config.setRemovePermission(toPermissions(legacyConfig.getRemovePermission(), config.getRemovePermission()));
        config.setTimePermission(toPermissions(legacyConfig.getTimePermission(), config.getTimePermission()));

        return config;
    }

    private static Set<String> toPermissions(String permission, Set<String> defaults) {
        if (permission == null || permission.isBlank()) {
            return defaults;
        }

        String trimmedPermission = permission.trim();
        LinkedHashSet<String> permissions = new LinkedHashSet<>();
        permissions.add(trimmedPermission);

        if (trimmedPermission.startsWith("wlbytime.")) {
            permissions.add("whitelistbytime." + trimmedPermission.substring("wlbytime.".length()));
        } else if (trimmedPermission.startsWith("whitelistbytime.")) {
            permissions.add("wlbytime." + trimmedPermission.substring("whitelistbytime.".length()));
        }

        return Set.copyOf(permissions);
    }
}
