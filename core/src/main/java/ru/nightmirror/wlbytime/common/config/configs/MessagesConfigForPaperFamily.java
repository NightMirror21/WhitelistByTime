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
public class MessagesConfigForPaperFamily extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .build();

    public MessagesConfigForPaperFamily() {
        super(MessagesConfigForPaperFamily.CONFIG);
    }

    @NewLine
    String pluginReloaded = "<gold>Plugin reloaded!";
    String notPermission = "<red>You do not have permission!";

    @NewLine
    String whitelistEnabled = "<aqua>WhitelistByTime enabled";
    String whitelistAlreadyEnabled = "<aqua>WhitelistByTime already enabled";
    String whitelistDisabled = "<aqua>WhitelistByTime disabled";
    String whitelistAlreadyDisabled = "<aqua>WhitelistByTime already disabled";

    @NewLine
    List<String> youNotInWhitelistKick = List.of(
            "<gold>Sorry, but you are not in whitelist",
            "Bye!"
    );

    @NewLine
    String playerRemovedFromWhitelist = "<yellow>%player% <white>successfully removed from whitelist";
    String playerAlreadyInWhitelist = "<yellow>%player% <white>already in whitelist";
    String playerNotInWhitelist = "<yellow>%player% <white>not in whitelist";

    @NewLine
    @Comment(value = {
            @CommentValue("For command with time")
    }, at = Comment.At.APPEND)
    String successfullyAddedForTime = "<aqua>%player% <white>added to whitelist for <aqua>%time%";
    String stillInWhitelistForTime = "<aqua>%player% <white>will be in whitelist still <aqua>%time%";
    String checkmeStillInWhitelistForTime = "<white>You will remain on the whitelist for <aqua>%time%";

    @NewLine
    @Comment(value = {
            @CommentValue("For command without time")
    }, at = Comment.At.APPEND)
    String successfullyAdded = "<aqua>%player% <white>added to whitelist forever";
    String stillInWhitelist = "<aqua>%player% <white>will be in whitelist forever";
    String checkMeStillInWhitelist = "<white>You are permanently whitelisted";

    @NewLine
    String listTitle = "<aqua>> Whitelist:";
    String listPlayer = "<aqua>| <white>%player% <gray>[%time%]";
    String listEmpty = "<aqua>Whitelist is empty";

    @NewLine
    String setTime = "Now <aqua>%player% <white>will be in whitelist for <aqua>%time%";
    String addTime = "Added <aqua>%time% <white>to <aqua>%player%";
    String removeTime = "Removed <aqua>%time% <white>from <aqua>%player%";

    @NewLine
    String forever = "forever";

    @NewLine
    List<String> help = List.of(
            "<aqua>> WhitelistByTime - Help",
            "<aqua>| <white>/whitelist on/off",
            "<aqua>| <white>/whitelist add [nickname] (time)",
            "<aqua>| <white>/whitelist remove [nickname]",
            "<aqua>| <white>/whitelist check [nickname]",
            "<aqua>| <white>/whitelist checkme",
            "<aqua>| <white>/whitelist reload",
            "<aqua>| <white>/whitelist getall",
            "<aqua>| <white>/whitelist time set/add/remove [nickname] [time]",
            "<aqua>| <white>(time) - time for which the player will be added to the whitelist",
            "<aqua>| <white>Example: 2d 3h 10m",
            "<aqua>| <white>Leave this value empty if you want to add player forever"
    );
}
