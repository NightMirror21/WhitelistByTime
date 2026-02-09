package ru.nightmirror.wlbytime.identity;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayerIdentityResolverImplTest {

    @Test
    public void resolveByNicknameUsesOnlinePlayer() {
        SettingsConfig settings = mock(SettingsConfig.class);
        when(settings.getPlayerIdMode()).thenReturn(PlayerIdMode.ONLINE);
        when(settings.isMojangLookupEnabled()).thenReturn(true);

        Server server = mock(Server.class);
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(server.getPlayerExact("Steve")).thenReturn(player);

        PlayerIdentityResolverImpl resolver = new PlayerIdentityResolverImpl(settings, server, Logger.getAnonymousLogger());

        ResolvedPlayer resolved = resolver.resolveByNickname("Steve");

        assertEquals(PlayerKey.uuid(uuid), resolved.key());
        assertEquals(uuid, resolved.uuid());
    }

    @Test
    public void resolveByNicknameFallsBackToMojangLookup() throws Exception {
        SettingsConfig settings = mock(SettingsConfig.class);
        when(settings.getPlayerIdMode()).thenReturn(PlayerIdMode.ONLINE);
        when(settings.isMojangLookupEnabled()).thenReturn(true);

        Server server = mock(Server.class);
        when(server.getPlayerExact("Steve")).thenReturn(null);
        OfflinePlayer offline = mock(OfflinePlayer.class);
        when(offline.hasPlayedBefore()).thenReturn(false);
        when(server.getOfflinePlayer("Steve")).thenReturn(offline);

        PlayerIdentityResolverImpl resolver = new PlayerIdentityResolverImpl(settings, server, Logger.getAnonymousLogger());
        MojangApiClient mojang = mock(MojangApiClient.class);
        UUID uuid = UUID.randomUUID();
        when(mojang.lookupUuid("Steve")).thenReturn(Optional.of(uuid));
        setField(resolver, "mojangApiClient", mojang);

        ResolvedPlayer resolved = resolver.resolveByNickname("Steve");

        assertEquals(PlayerKey.uuid(uuid), resolved.key());
        assertEquals(uuid, resolved.uuid());
    }

    @Test
    public void resolveByIssuerOfflineModeReturnsNickname() {
        SettingsConfig settings = mock(SettingsConfig.class);
        when(settings.getPlayerIdMode()).thenReturn(PlayerIdMode.OFFLINE);
        Server server = mock(Server.class);
        CommandIssuer issuer = mock(CommandIssuer.class);
        when(issuer.getNickname()).thenReturn("Alex");

        PlayerIdentityResolverImpl resolver = new PlayerIdentityResolverImpl(settings, server, Logger.getAnonymousLogger());
        ResolvedPlayer resolved = resolver.resolveByIssuer(issuer);

        assertEquals(PlayerKey.nickname("Alex"), resolved.key());
        assertEquals("Alex", resolved.nickname());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
