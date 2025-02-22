package ru.nightmirror.wlbytime.interfaces.parser;

public interface PlaceholderParser {
    String parse(String playerNickname, String params);

    String getEmpty();
}
