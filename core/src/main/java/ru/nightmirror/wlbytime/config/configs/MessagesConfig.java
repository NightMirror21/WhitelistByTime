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
    String incorrectArguments = "Incorrect argument(s)";

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
    String playerRemovedFromWhitelist = "%nickname% successfully removed from whitelist";
    String playerAlreadyInWhitelist = "%nickname% already in whitelist";
    String playerNotInWhitelist = "%nickname% not in whitelist";
    String checkMeNotInWhitelist = "You are not in whitelist";
    String checkMeFrozen = "You are frozen for %time%";

    @NewLine
    @Comment(value = {
            @CommentValue("For command with time")
    }, at = Comment.At.PREPEND)
    String successfullyAddedForTime = "%nickname% added to whitelist for %time%";
    String stillInWhitelistForTime = "%nickname% will be in whitelist still %time%";
    String checkMeStillInWhitelistForTime = "You will remain on the whitelist for %time%";

    @NewLine
    @Comment(value = {
            @CommentValue("For command without time")
    }, at = Comment.At.PREPEND)
    String successfullyAdded = "%nickname% added to whitelist forever";
    String stillInWhitelist = "%nickname% will be in whitelist forever";
    String checkMeStillInWhitelistForever = "You are permanently whitelisted";

    @NewLine
    String listHeader = "> Whitelist:";
    String listElement = "| %nickname% [%time-or-status%]";
    String listEmpty = "Whitelist is empty";
    String listFooter = "Page %current-page% / %max-page% (To show another page run /whitelist getall <page>)";
    String pageNotExists = "Page %page% not exists, max page is %max-page%";

    @NewLine
    @Comment(value = {
            @CommentValue("How many records will be displayed per page")
    }, at = Comment.At.PREPEND)
    int entriesForPage = 10;

    @NewLine
    @Comment(value = {
            @CommentValue("For '%time-or-status%' in list")
    }, at = Comment.At.PREPEND)
    String forever = "forever";
    String frozen = "frozen for %time%";
    String active = "active for %time%";
    String expired = "expired";

    @NewLine
    String setTime = "Now %nickname% will be in whitelist for %time%";
    String addTime = "Added %time% to %nickname%";
    String removeTime = "Removed %time% from %nickname%";
    String cantAddTimeCausePlayerIsForever = "Can't add time cause %nickname% is forever";
    String cantRemoveTimeCausePlayerIsForever = "Can't add time cause %nickname% is forever";
    String timeIsIncorrect = "Time is incorrect";
    String cantAddTime = "Can't add time";
    String cantRemoveTime = "Can't remove time";

    @NewLine
    String playerUnfrozen = "Player %nickname% unfrozen";
    String playerFrozen = "Player %nickname% frozen for %time%";
    String playerAlreadyFrozen = "Player %nickname% already frozen";
    String playerNotFrozen = "Player %nickname% not frozen";
    String playerExpired = "Player %nickname% expired";

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
