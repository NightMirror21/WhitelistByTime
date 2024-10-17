package ru.nightmirror.wlbytime.common.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.List;

@UtilityClass
public class ComponentUtils {
    private static final Component NEWLINE_DELIMITER = Component.text("\n");

    public static Component join(List<Component> components) {
        TextComponent.Builder componentBuilder = Component.text();
        appendComponentsWithDelimiter(components, componentBuilder);
        return componentBuilder.build();
    }

    private static void appendComponentsWithDelimiter(List<Component> components, TextComponent.Builder builder) {
        for (int i = 0; i < components.size(); i++) {
            builder.append(components.get(i));
            if (i < components.size() - 1) {
                builder.append(ComponentUtils.NEWLINE_DELIMITER);
            }
        }
    }
}
