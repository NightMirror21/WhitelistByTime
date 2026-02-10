package ru.nightmirror.wlbytime.interfaces.command;

import java.util.Set;

public interface Command {
    Set<String> getPermissions();

    String getName();

    void execute(CommandIssuer issuer, String[] args);

    Set<String> getTabulate(CommandIssuer issuer, String[] args);
}
