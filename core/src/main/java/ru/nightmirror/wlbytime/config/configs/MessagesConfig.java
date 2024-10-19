package ru.nightmirror.wlbytime.config.configs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@SuppressWarnings("FieldMayBeFinal")
public class MessagesConfig extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .build();

    String pluginReloaded = "Plugin reloaded!";
    String notPermission = "You do not have permission!";

    @NewLine
    String whitelistEnabled = "WhitelistByTime enabled";
    String whitelistAlreadyEnabled = "WhitelistByTime already enabled";
    String whitelistDisabled = "WhitelistByTime disabled";
    String whitelistAlreadyDisabled = "WhitelistByTime already disabled";

    @NewLine
    List<String> youNotInWhitelistKick = List.of(
            "Sorry, but you are not in whitelist",
            "Bye!"
    );

    @NewLine
    String playerRemovedFromWhitelist = "%player% successfully removed from whitelist";
    String playerAlreadyInWhitelist = "%player% already in whitelist";
    String playerNotInWhitelist = "%player% not in whitelist";

    @NewLine
    @Comment(value = {
            @CommentValue("For command with time")
    }, at = Comment.At.PREPEND)
    String successfullyAddedForTime = "%player% added to whitelist for %time%";
    String stillInWhitelistForTime = "%player% will be in whitelist still %time%";
    String checkMeStillInWhitelistForTime = "You will remain on the whitelist for %time%";

    @NewLine
    @Comment(value = {
            @CommentValue("For command without time")
    }, at = Comment.At.PREPEND)
    String successfullyAdded = "%player% added to whitelist forever";
    String stillInWhitelist = "%player% will be in whitelist forever";
    String checkMeStillInWhitelist = "You are permanently whitelisted";

    @NewLine
    String listTitle = "> Whitelist:";
    String listPlayer = "| %player% [%time%]";
    String listEmpty = "Whitelist is empty";
    String listPageableCommands = "Page %current-page% / %max-page% (To show another page run /whitelist getall <page>)";
    String pageNotExists = "Page %page% not exists";

    @NewLine
    String setTime = "Now %player% will be in whitelist for %time%";
    String addTime = "Added %time% to %player%";
    String removeTime = "Removed %time% from %player%";

    @NewLine
    String forever = "forever";
    String frozen = "frozen";
    String expired = "expired";

    @NewLine
    String playerUnfrozen = "Player %player% unfrozen";
    String playerFrozen = "Player %player% frozen";

    @NewLine
    List<String> help = List.of(
            "> WhitelistByTime - Help",
            "| /whitelist on/off",
            "| /whitelist add [nickname] (time)",
            "| /whitelist remove [nickname]",
            "| /whitelist check [nickname]",
            "| /whitelist checkme",
            "| /whitelist reload",
            "| /whitelist getall",
            "| /whitelist switchfreeze [nickname]",
            "| /whitelist time set/add/remove [nickname] [time]",
            "| (time) - time for which the player will be added to the whitelist",
            "| Example: 2d 3h 10m",
            "| Leave this value empty if you want to add player forever"
    );

    public MessagesConfig() {
        super(MessagesConfig.CONFIG);
    }
}
