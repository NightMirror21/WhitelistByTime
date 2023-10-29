package ru.nightmirror.wlbytime.common.config.configs;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class SettingsConfig extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .build();

    public SettingsConfig() {
        super(SettingsConfig.CONFIG);
    }

    @NewLine
    @Comment(value = {
            @CommentValue("Is whitelist enabled by default on startup")
    }, at = Comment.At.APPEND)
    boolean enabled = true;

    @NewLine
    @Comment(value = {
            @CommentValue("The delay through which the thread will check players. In milliseconds")
    }, at = Comment.At.APPEND)
    int checkerDelay = 1000;

    @NewLine
    @Comment(value = {
            @CommentValue("Check the case of the nickname")
    }, at = Comment.At.APPEND)
    boolean caseSensitive = true;
}
