package ru.nightmirror.wlbytime.consumers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import ru.nightmirror.wlbytime.utils.ColorsUtils;

import java.util.Collections;
import java.util.function.BiConsumer;

public class MessageSender implements BiConsumer<String, String> {
    @Override
    public void accept(String nickname, String message) {
        Component component = ColorsUtils.convertMessage(message);
        Collections.unmodifiableCollection(Bukkit.getOnlinePlayers()).stream()
                .filter(player -> player.getName().equalsIgnoreCase(nickname))
                .forEach(player -> player.sendMessage(component));
    }
}
