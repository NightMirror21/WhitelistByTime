package ru.nightmirror.wlbytime.api.events;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.nightmirror.wlbytime.api.WhitelistByTimeAPI;

public class RemoveAllFromWhitelistCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (String nickname : WhitelistByTimeAPI.getAllPlayers()) {
            WhitelistByTimeAPI.removePlayer(nickname);
        }

        commandSender.sendMessage("Success!");
        return true;
    }
}
