package ru.nightmirror.wlbytime.common.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.List;

public class ComponentUtils {
    public static Component join(List<Component> components, Component delimiter) {
        TextComponent.Builder builder = Component.text();

        for (int i = 0; i < components.size(); i++) {
            builder.append(components.get(i));

            if (i < components.size() - 1) {
                builder.append(delimiter);
            }
        }

        return builder.build();
    }
}
