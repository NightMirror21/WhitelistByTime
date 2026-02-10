package ru.nightmirror.wlbytime.utils;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComponentUtilsTest {

    @Test
    public void joinAddsNewlineBetweenComponents() {
        Component first = Component.text("one");
        Component second = Component.text("two");
        Component expected = Component.text().append(first).append(Component.text("\n")).append(second).build();

        Component result = ComponentUtils.join(List.of(first, second));

        assertEquals(expected, result);
    }
}
