package ru.nightmirror.wlbytime.config.configs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@SuppressWarnings("FieldMayBeFinal")
public class SettingsConfig extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .build();

    public SettingsConfig() {
        super(SettingsConfig.CONFIG);
    }

    @Comment(value = {
            @CommentValue("Automatically unfreeze player time when they join the server if their time is frozen")
    }, at = Comment.At.PREPEND)
    boolean unfreezeTimeOnPlayerJoin = false;

    @NewLine
    @Comment(value = {
            @CommentValue("Enable the expiration monitor, which checks players' expiration status")
    }, at = Comment.At.PREPEND)
    boolean expireMonitorEnabled = true;

    @Comment(value = {
            @CommentValue("Interval in milliseconds for the expiration monitor to check players and remove them from the database if expired")
    }, at = Comment.At.PREPEND)
    int expireMonitorIntervalMs = 5000;

    @NewLine
    @Comment(value = {
            @CommentValue("Enable the last join monitor, which checks players' last join timestamps")
    }, at = Comment.At.PREPEND)
    boolean lastJoinMonitorEnabled = false;

    @Comment(value = {
            @CommentValue("Threshold in seconds between the player's last join and the current time. If exceeded, the player is removed by the monitor")
    })
    int lastJoinExpirationThresholdSeconds = 3600 * 24 * 31;

    @Comment(value = {
            @CommentValue("Interval in milliseconds for the last join monitor to check players and remove them if their last join exceeds the threshold")
    }, at = Comment.At.PREPEND)
    int lastJoinMonitorIntervalMs = 3600000;

    @NewLine
    @Comment(value = {
            @CommentValue("Enable case-sensitive nickname checking")
    }, at = Comment.At.PREPEND)
    boolean nicknameCaseSensitive = true;

    @NewLine
    @Comment(value = {
            @CommentValue("Kick player from server when his time is expired")
    }, at = Comment.At.PREPEND)
    boolean kickPlayerOnTimeExpire = true;

    @NewLine
    @Comment(value = {
            @CommentValue("Remind players how much time they have left on the whitelist. Doesn't work if the player is permanently whitelisted or frozen.")
    }, at = Comment.At.PREPEND)
    boolean notifyPlayersHowMuchLeft = false;

    @Comment(value = {
            @CommentValue("Interval in milliseconds for the plugin to send reminders to players how much time they have left on the whitelist.")
    })
    int notifyPlayerMonitorIntervalMs = 900 * 1000;

    @Comment(value = {
            @CommentValue("Time-left threshold (in seconds)."),
            @CommentValue("If a player has less time than this before their whitelist entry expires,"),
            @CommentValue("the plugin will start sending reminders. Example: 3600 = remind when < 1 hour left.")
    }, at = Comment.At.PREPEND)
    int notifyPlayerTimeLeftThresholdSeconds = 3600;


    @NewLine
    @Comment(value = {
            @CommentValue("Symbols representing time units for years")
    }, at = Comment.At.PREPEND)
    Set<String> yearTimeUnits = Set.of("y");

    @Comment(value = {
            @CommentValue("Symbols representing time units for months")
    })
    Set<String> monthTimeUnits = Set.of("mo");

    @Comment(value = {
            @CommentValue("Symbols representing time units for weeks")
    })
    Set<String> weekTimeUnits = Set.of("w");

    @Comment(value = {
            @CommentValue("Symbols representing time units for days")
    })
    Set<String> dayTimeUnits = Set.of("d");

    @Comment(value = {
            @CommentValue("Symbols representing time units for hours")
    })
    Set<String> hourTimeUnits = Set.of("h");

    @Comment(value = {
            @CommentValue("Symbols representing time units for minutes")
    })
    Set<String> minuteTimeUnits = Set.of("m");

    @Comment(value = {
            @CommentValue("Symbols representing time units for seconds")
    })
    Set<String> secondTimeUnits = Set.of("s");
}
