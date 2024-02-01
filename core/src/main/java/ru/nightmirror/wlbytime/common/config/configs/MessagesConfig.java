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
public class MessagesConfig extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .build();

    String pluginReloaded = "&6Plugin reloaded!";
    String notPermission = "&cYou do not have permission!";

    @NewLine
    String whitelistEnabled = "&bWhitelistByTime enabled";
    String whitelistAlreadyEnabled = "&bWhitelistByTime already enabled";
    String whitelistDisabled = "&bWhitelistByTime disabled";
    String whitelistAlreadyDisabled = "&bWhitelistByTime already disabled";

    @NewLine
    List<String> youNotInWhitelistKick = List.of(
            "&6Sorry, but you are not in whitelist",
            "Bye!"
    );

    @NewLine
    String playerRemovedFromWhitelist = "&e%player% &fsuccessfully removed from whitelist";
    String playerAlreadyInWhitelist = "&e%player% &falready in whitelist";
    String playerNotInWhitelist = "&e%player% &fnot in whitelist";

    @NewLine
    @Comment(value = {
            @CommentValue("For command with time")
    }, at = Comment.At.PREPEND)
    String successfullyAddedForTime = "&b%player% &fadded to whitelist for &b%time%";
    String stillInWhitelistForTime = "&b%player% &fwill be in whitelist still &b%time%";
    String checkmeStillInWhitelistForTime = "&fYou will remain on the whitelist for &b%time%";

    @NewLine
    @Comment(value = {
            @CommentValue("For command without time")
    }, at = Comment.At.PREPEND)
    String successfullyAdded = "&b%player% &fadded to whitelist forever";
    String stillInWhitelist = "&b%player% &fwill be in whitelist forever";
    String checkMeStillInWhitelist = "&fYou are permanently whitelisted";

    @NewLine
    String listTitle = "&b> Whitelist:";
    String listPlayer = "&b| &f%player% <gray>[%time%]";
    String listEmpty = "&bWhitelist is empty";

    @NewLine
    String setTime = "Now &b%player% &fwill be in whitelist for &b%time%";
    String addTime = "Added &b%time% &fto &b%player%";
    String removeTime = "Removed &b%time% &ffrom &b%player%";

    @NewLine
    String forever = "forever";

    @NewLine
    List<String> help = List.of(
            "&b> WhitelistByTime - Help",
            "&b| &f/whitelist on/off",
            "&b| &f/whitelist add [nickname] (time)",
            "&b| &f/whitelist remove [nickname]",
            "&b| &f/whitelist check [nickname]",
            "&b| &f/whitelist checkme",
            "&b| &f/whitelist reload",
            "&b| &f/whitelist getall",
            "&b| &f/whitelist time set/add/remove [nickname] [time]",
            "&b| &f(time) - time for which the player will be added to the whitelist",
            "&b| &fExample: 2d 3h 10m",
            "&b| &fLeave this value empty if you want to add player forever"
    );

    public MessagesConfig() {
        super(MessagesConfig.CONFIG);
    }
}
