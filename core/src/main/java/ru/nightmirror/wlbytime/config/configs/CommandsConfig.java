package ru.nightmirror.wlbytime.config.configs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class CommandsConfig extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .build();

    @Comment(value = {
            @CommentValue("Permissions for whitelist subcommands")
    }, at = Comment.At.PREPEND)
    String addPermission = "wlbytime.add";
    String checkPermission = "wlbytime.check";
    String checkMePermission = "wlbytime.checkme";
    String freezePermission = "wlbytime.freeze";
    String getAllPermission = "wlbytime.getall";
    String togglePermission = "wlbytime.toggle";
    String removePermission = "wlbytime.remove";
    String timePermission = "wlbytime.time";

    public CommandsConfig() {
        super(CommandsConfig.CONFIG);
    }
}
