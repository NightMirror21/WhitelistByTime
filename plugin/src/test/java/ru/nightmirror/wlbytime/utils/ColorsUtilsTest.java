package ru.nightmirror.wlbytime.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColorsUtilsTest {

    @Test
    public void convertMessageReplacesLegacyColors() {
        Component expected = MiniMessage.miniMessage().deserialize("<green>Hi");
        Component actual = ColorsUtils.convertMessage("&aHi");
        assertEquals(expected, actual);
    }

    @Test
    public void convertReturnsComponentList() {
        List<Component> components = ColorsUtils.convert(List.of("one", "two"));
        assertEquals(2, components.size());
    }
}
