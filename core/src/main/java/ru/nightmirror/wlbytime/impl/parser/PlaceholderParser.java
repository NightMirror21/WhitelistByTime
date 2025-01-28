package ru.nightmirror.wlbytime.impl.parser;

public interface PlaceholderParser {
    String parse(String playerNickname, String params);

    String getEmpty();
}
