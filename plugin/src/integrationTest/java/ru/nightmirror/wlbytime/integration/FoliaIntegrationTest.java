package ru.nightmirror.wlbytime.integration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.factory.ClientNetworkSessionFactory;
import org.geysermc.mcprotocollib.network.session.ClientNetworkSession;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FoliaIntegrationTest {

    private static final String RCON_PASSWORD = "testpass";
    private static final int RCON_PORT = 25575;
    private static final String DATA_DIR = "/data";
    private static final String STAGING_PLUGINS_DIR = "/plugins";
    private static final String PLUGIN_NAME = "WhitelistByTime";
    private static final String DB_FILE = "wlbytime.db";
    private static final long MIN_LOGIN_GAP_MS = 15000;
    private static final AtomicLong LAST_LOGIN_MS = new AtomicLong(0);

    private static final Path PLUGIN_JAR = resolvePluginJar();

    @Container
    private static final GenericContainer<?> SERVER = new GenericContainer<>("itzg/minecraft-server:latest")
            .withEnv("EULA", "TRUE")
            .withEnv("TYPE", "FOLIA")
            .withEnv("VERSION", System.getProperty("foliaVersion", "1.21.11"))
            .withEnv("ONLINE_MODE", "FALSE")
            .withEnv("ENABLE_RCON", "true")
            .withEnv("RCON_PASSWORD", RCON_PASSWORD)
            .withEnv("RCON_PORT", String.valueOf(RCON_PORT))
            .withEnv("RCON_HOST", "localhost")
            .withEnv("CONNECTION_THROTTLE", "0")
            .withExposedPorts(25565)
            .withCopyFileToContainer(MountableFile.forHostPath(PLUGIN_JAR), STAGING_PLUGINS_DIR + "/" + PLUGIN_NAME + ".jar")
            .waitingFor(Wait.forLogMessage(".*Done \\(.*\\)!.*", 1).withStartupTimeout(Duration.ofMinutes(10)));

    @BeforeAll
    public void waitForPluginEnabled() {
        waitForLogContains(SERVER, "Plugin enabled", Duration.ofMinutes(2));
    }

    @Test
    public void pluginCreatesConfigFiles() throws Exception {
        Path tempDir = Files.createTempDirectory("wlbytime-configs");
        String base = DATA_DIR + "/plugins/" + PLUGIN_NAME + "/";

        String[] configFiles = new String[]{
                "messages.yml",
                "database.yml",
                "placeholders.yml",
                "settings.yml",
                "commands.yml"
        };

        for (String file : configFiles) {
            Path out = tempDir.resolve(file);
            SERVER.copyFileFromContainer(base + file, out.toString());
            Assertions.assertThat(Files.size(out))
                    .as("Config file %s should be non-empty", file)
                    .isGreaterThan(0);
        }

        Path dbOut = tempDir.resolve(DB_FILE);
        SERVER.copyFileFromContainer(base + DB_FILE, dbOut.toString());
        Assertions.assertThat(Files.size(dbOut))
                .as("Database file should be non-empty")
                .isGreaterThan(0);
    }

    @Test
    public void whitelistStatusCommandWorks() throws Exception {
        var result = SERVER.execInContainer("rcon-cli", "wlbytime", "status");
        Assertions.assertThat(result.getExitCode()).isEqualTo(0);
        Assertions.assertThat(result.getStdout())
                .contains("Whitelist is");
    }

    @Test
    public void addCommandCreatesEntryInDatabase() throws Exception {
        String nickname = "TestUser" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        var addResult = SERVER.execInContainer("rcon-cli", "wlbytime", "add", nickname, "1m");
        Assertions.assertThat(addResult.getExitCode()).isEqualTo(0);

        Path tempDb = Files.createTempFile("wlbytime", ".db");
        SERVER.copyFileFromContainer(DATA_DIR + "/plugins/" + PLUGIN_NAME + "/" + DB_FILE, tempDb.toString());

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + tempDb.toAbsolutePath())) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM wlbytime_entries WHERE nickname = ?")) {
                stmt.setString(1, nickname);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    int count = rs.getInt(1);
                    Assertions.assertThat(count)
                            .as("Entry should be created for nickname %s", nickname)
                            .isEqualTo(1);
                }
            }
        }
    }

    @Test
    public void loginIsBlockedWhenNotWhitelisted() throws Exception {
        ensureWhitelistEnabled(SERVER);
        String nickname = "LoginTest" + shortId();
        LoginResult result = attemptLogin(SERVER, nickname, Duration.ofSeconds(12));
        assertDenied(result);
    }

    @Test
    public void loginAllowedWhenWhitelistDisabled() throws Exception {
        ensureWhitelistDisabled(SERVER);
        String nickname = "NoWhitelist" + shortId();
        LoginResult result = attemptLogin(SERVER, nickname, Duration.ofSeconds(2));
        assertAllowed(result);
    }

    @Test
    public void loginAllowedWhenWhitelistedForever() throws Exception {
        ensureWhitelistEnabled(SERVER);
        String nickname = "Forever" + shortId();
        runCommand(SERVER, "wlbytime", "add", nickname);
        LoginResult result = attemptLogin(SERVER, nickname, Duration.ofSeconds(2));
        assertAllowed(result);
    }

    @Test
    public void loginAllowedWhenWhitelistedWithTime() throws Exception {
        ensureWhitelistEnabled(SERVER);
        String nickname = "Timed" + shortId();
        runCommand(SERVER, "wlbytime", "add", nickname, "2m");
        LoginResult result = attemptLogin(SERVER, nickname, Duration.ofSeconds(2));
        assertAllowed(result);
    }

    @Test
    public void loginDeniedWhenWhitelistedExpired() throws Exception {
        ensureWhitelistEnabled(SERVER);
        String nickname = "Expired" + shortId();
        runCommand(SERVER, "wlbytime", "add", nickname, "1s");
        sleep(Duration.ofSeconds(2));
        LoginResult result = attemptLogin(SERVER, nickname, Duration.ofSeconds(8));
        assertDenied(result);
    }

    @Test
    public void loginDeniedWhenFrozen() throws Exception {
        ensureWhitelistEnabled(SERVER);
        String nickname = "Frozen" + shortId();
        runCommand(SERVER, "wlbytime", "add", nickname, "20s");
        String freezeOut = runCommandWithOutput(SERVER, "wlbytime", "freeze", nickname, "60s");
        Assertions.assertThat(freezeOut)
                .as("Freeze command should report frozen state")
                .containsIgnoringCase("frozen");
        String checkOut = runCommandWithOutput(SERVER, "wlbytime", "check", nickname);
        Assertions.assertThat(checkOut)
                .as("Check command should report frozen state")
                .containsIgnoringCase("frozen");
        LoginResult result = attemptLogin(SERVER, nickname, Duration.ofSeconds(8));
        assertDenied(result);
    }

    @Test
    public void loginAllowedWhenFrozenButUnfreezeOnJoinEnabled() throws Exception {
        Path dataDir = Files.createTempDirectory("wlbytime-unfreeze");
        Path settingsDir = dataDir.resolve("plugins").resolve(PLUGIN_NAME);
        Files.createDirectories(settingsDir);
        Files.writeString(settingsDir.resolve("settings.yml"),
                "unfreeze-time-on-player-join: true\n" +
                "player-id-mode: OFFLINE\n" +
                "mojang-lookup-enabled: false\n" +
                "whitelist-enabled: true\n");

        try (GenericContainer<?> unfreezeServer = new GenericContainer<>("itzg/minecraft-server:latest")
                .withFileSystemBind(dataDir.toAbsolutePath().toString(), DATA_DIR)
                .withEnv("EULA", "TRUE")
                .withEnv("TYPE", "FOLIA")
                .withEnv("VERSION", System.getProperty("foliaVersion", "1.21.11"))
                .withEnv("ONLINE_MODE", "FALSE")
                .withEnv("ENABLE_RCON", "true")
                .withEnv("RCON_PASSWORD", RCON_PASSWORD)
                .withEnv("RCON_PORT", String.valueOf(RCON_PORT))
                .withEnv("RCON_HOST", "localhost")
                .withEnv("CONNECTION_THROTTLE", "0")
                .withExposedPorts(25565)
                .withCopyFileToContainer(MountableFile.forHostPath(PLUGIN_JAR), STAGING_PLUGINS_DIR + "/" + PLUGIN_NAME + ".jar")
                .waitingFor(Wait.forLogMessage(".*Done \\(.*\\)!.*", 1).withStartupTimeout(Duration.ofMinutes(6)))) {
            unfreezeServer.start();
            waitForLogContains(unfreezeServer, "Plugin enabled", Duration.ofMinutes(2));
            Path tempSettings = Files.createTempFile("wlbytime-settings", ".yml");
            unfreezeServer.copyFileFromContainer(DATA_DIR + "/plugins/" + PLUGIN_NAME + "/settings.yml", tempSettings.toString());
            String settingsText = Files.readString(tempSettings);
            Assertions.assertThat(settingsText)
                    .as("Unfreeze setting should be enabled in settings.yml")
                    .contains("unfreeze-time-on-player-join: true");

            String nickname = "Unfreeze" + shortId();
            runCommand(unfreezeServer, "wlbytime", "add", nickname, "20s");
            runCommand(unfreezeServer, "wlbytime", "freeze", nickname, "60s");
            LoginResult result = attemptLogin(unfreezeServer, nickname, Duration.ofSeconds(2));
            assertAllowed(result);
        }
    }

    private static Path resolvePluginJar() {
        String jar = System.getProperty("pluginJar");
        if (jar == null || jar.isBlank()) {
            throw new IllegalStateException("System property 'pluginJar' is not set. Run :plugin:integrationTest.");
        }
        Path path = Paths.get(jar);
        if (!Files.exists(path)) {
            throw new IllegalStateException("Plugin jar not found at: " + path);
        }
        return path;
    }

    private static void waitForLogContains(GenericContainer<?> server, String needle, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (server.getLogs().contains(needle)) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for server logs", e);
            }
        }
        throw new IllegalStateException("Did not find log line: " + needle + "\nLogs:\n" + server.getLogs());
    }

    private static void ensureWhitelistEnabled(GenericContainer<?> server) throws Exception {
        runCommand(server, "wlbytime", "on");
    }

    private static void ensureWhitelistDisabled(GenericContainer<?> server) throws Exception {
        runCommand(server, "wlbytime", "off");
    }

    private static void runCommand(GenericContainer<?> server, String... command) throws Exception {
        runCommandWithOutput(server, command);
    }

    private static String runCommandWithOutput(GenericContainer<?> server, String... command) throws Exception {
        var result = server.execInContainer(merge("rcon-cli", command));
        if (result.getExitCode() != 0) {
            throw new IllegalStateException("Command failed: " + String.join(" ", command) + "\n" + result.getStderr());
        }
        String output = (result.getStdout() == null ? "" : result.getStdout()).trim();
        if (output.toLowerCase().contains("unknown command")) {
            throw new IllegalStateException("Unknown command: " + String.join(" ", command));
        }
        return output;
    }

    private static LoginResult attemptLogin(GenericContainer<?> server, String nickname, Duration wait) throws Exception {
        LoginResult first = doLogin(server, nickname, wait);
        if (first.disconnected && isThrottle(first.reason)) {
            sleep(Duration.ofMillis(MIN_LOGIN_GAP_MS));
            return doLogin(server, nickname, wait);
        }
        return first;
    }

    private static LoginResult doLogin(GenericContainer<?> server, String nickname, Duration wait) throws Exception {
        String host = server.getHost();
        int port = server.getMappedPort(25565);

        throttleLoginAttempts();
        CountDownLatch disconnected = new CountDownLatch(1);
        AtomicReference<String> reason = new AtomicReference<>();
        AtomicReference<Instant> disconnectedAt = new AtomicReference<>();

        MinecraftProtocol protocol = new MinecraftProtocol(nickname);
        protocol.setUseDefaultListeners(true);
        ClientNetworkSession session = ClientNetworkSessionFactory.factory()
                .setAddress(host, port)
                .setProtocol(protocol)
                .create();
        session.addListener(new SessionAdapter() {
            @Override
            public void disconnected(DisconnectedEvent event) {
                String plain = PlainTextComponentSerializer.plainText().serialize(event.getReason());
                reason.set(plain);
                disconnectedAt.set(Instant.now());
                disconnected.countDown();
            }
        });

        session.connect(true);
        boolean wasDisconnected = disconnected.await(wait.toMillis(), TimeUnit.MILLISECONDS);
        if (!wasDisconnected) {
            session.disconnect("test-complete");
        }
        return new LoginResult(wasDisconnected, reason.get(), disconnectedAt.get());
    }

    private static void assertDenied(LoginResult result) {
        Assertions.assertThat(result.disconnected)
                .as("Login should be rejected")
                .isTrue();
        Assertions.assertThat(result.reason)
                .as("Kick reason should mention whitelist or frozen")
                .containsIgnoringCase("whitelist");
    }

    private static void assertAllowed(LoginResult result) {
        Assertions.assertThat(result.reason)
                .as("Login should not be rejected by whitelist. Reason: %s", result.reason)
                .doesNotContainIgnoringCase("whitelist");
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting", e);
        }
    }

    private static void throttleLoginAttempts() {
        long now = System.currentTimeMillis();
        long last = LAST_LOGIN_MS.getAndSet(now);
        long elapsed = now - last;
        if (last != 0 && elapsed < MIN_LOGIN_GAP_MS) {
            sleep(Duration.ofMillis(MIN_LOGIN_GAP_MS - elapsed));
        }
    }

    private static boolean isThrottle(String reason) {
        return reason != null && reason.toLowerCase().contains("throttled");
    }

    private static String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    private static String[] merge(String first, String... rest) {
        String[] merged = new String[rest.length + 1];
        merged[0] = first;
        System.arraycopy(rest, 0, merged, 1, rest.length);
        return merged;
    }

    private static final class LoginResult {
        final boolean disconnected;
        final String reason;
        final Instant disconnectedAt;

        private LoginResult(boolean disconnected, String reason, Instant disconnectedAt) {
            this.disconnected = disconnected;
            this.reason = reason;
            this.disconnectedAt = disconnectedAt;
        }
    }
}
