package ru.nightmirror.wlbytime.interfaces.identity;

import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.identity.ResolvedPlayer;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;

import java.util.UUID;

public interface PlayerIdentityResolver {
    @NotNull ResolvedPlayer resolveByNickname(@NotNull String nickname);

    @NotNull ResolvedPlayer resolveByIssuer(@NotNull CommandIssuer issuer);

    @NotNull ResolvedPlayer resolveByLogin(@NotNull String nickname, @NotNull UUID uuid);
}
