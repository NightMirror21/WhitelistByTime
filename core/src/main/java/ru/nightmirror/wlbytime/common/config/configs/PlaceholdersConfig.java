package ru.nightmirror.wlbytime.common.config.configs;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class PlaceholdersConfig extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .build();

    boolean placeholdersEnabled = false;

    @NewLine
    @Comment(value = {
            @CommentValue("%wlbytime_in_whitelist% - In whitelist or not")
    }, at = Comment.At.APPEND)
    String inWhitelistTrue = "<green>✔";
    String inWhitelistFalse = "<red>✖";

    @NewLine
    @Comment(value = {
            @CommentValue("%wlbytime_time_left% - How much is left in whitelist")
    }, at = Comment.At.APPEND)
    String timeLeft = "<green>%time%";

    public PlaceholdersConfig() {
        super(PlaceholdersConfig.CONFIG);
    }
}
