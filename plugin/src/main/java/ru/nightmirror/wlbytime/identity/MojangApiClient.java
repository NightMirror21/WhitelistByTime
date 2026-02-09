package ru.nightmirror.wlbytime.identity;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class MojangApiClient {

    private final HttpClient httpClient;
    private final int timeoutMs;
    private final boolean cacheEnabled;
    private final int cacheTtlMs;
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public MojangApiClient(int timeoutMs, boolean cacheEnabled, int cacheTtlMs) {
        this.httpClient = HttpClient.newHttpClient();
        this.timeoutMs = timeoutMs;
        this.cacheEnabled = cacheEnabled;
        this.cacheTtlMs = cacheTtlMs;
    }

    public Optional<UUID> lookupUuid(String nickname) {
        String key = nickname.toLowerCase(Locale.ROOT);
        if (cacheEnabled) {
            CacheEntry cached = cache.get(key);
            if (cached != null && !cached.isExpired()) {
                return Optional.ofNullable(cached.uuid);
            }
        }
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + nickname;
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.debug("Mojang API returned status={} for nickname={}", response.statusCode(), nickname);
                putCache(key, null);
                return Optional.empty();
            }
            String rawId = extractId(response.body());
            if (rawId == null || rawId.length() != 32) {
                log.debug("Mojang API response did not contain a valid id for nickname={}", nickname);
                putCache(key, null);
                return Optional.empty();
            }
            String dashed = rawId.substring(0, 8) + "-" + rawId.substring(8, 12) + "-" + rawId.substring(12, 16) + "-"
                    + rawId.substring(16, 20) + "-" + rawId.substring(20);
            UUID uuid = UUID.fromString(dashed.toLowerCase(Locale.ROOT));
            putCache(key, uuid);
            return Optional.of(uuid);
        } catch (Exception exception) {
            log.debug("Mojang API lookup failed for nickname={}", nickname, exception);
            putCache(key, null);
            return Optional.empty();
        }
    }

    private String extractId(String body) {
        int idIndex = body.indexOf("\"id\"");
        if (idIndex == -1) {
            return null;
        }
        int colon = body.indexOf(':', idIndex);
        if (colon == -1) {
            return null;
        }
        int quote1 = body.indexOf('"', colon + 1);
        if (quote1 == -1) {
            return null;
        }
        int quote2 = body.indexOf('"', quote1 + 1);
        if (quote2 == -1) {
            return null;
        }
        return body.substring(quote1 + 1, quote2);
    }

    private void putCache(String key, @Nullable UUID uuid) {
        if (!cacheEnabled) {
            return;
        }
        cache.put(key, new CacheEntry(uuid, Instant.now().plusMillis(cacheTtlMs)));
    }

    private static class CacheEntry {
        private final UUID uuid;
        private final Instant expiresAt;

        private CacheEntry(UUID uuid, Instant expiresAt) {
            this.uuid = uuid;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
