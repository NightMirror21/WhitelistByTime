package ru.nightmirror.wlbytime.common.config.configs;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.List;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class DatabaseConfig extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .build();

    @Comment(value = {
            @CommentValue("'sqlite' or 'mysql'")
    }, at = Comment.At.APPEND)
    String type = "sqlite";

    @NewLine
    @Comment(value = {
            @CommentValue("If not sqlite or h2")
    }, at = Comment.At.APPEND)
    String address = "localhost:3030";
    String name = "minecraft";

    @NewLine
    @Comment(value = {
            @CommentValue("Params for connection")
    }, at = Comment.At.APPEND)
    List<String> params = List.of("autoReconnect=true");

    @NewLine
    @Comment(value = {
            @CommentValue("If using user and password")
    }, at = Comment.At.APPEND)
    boolean useUserAndPassword = false;
    String user = "user";
    String password = "qwerty123";

    public DatabaseConfig() {
        super(DatabaseConfig.CONFIG);
    }
}
