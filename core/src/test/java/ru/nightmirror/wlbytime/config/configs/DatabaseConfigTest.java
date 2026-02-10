package ru.nightmirror.wlbytime.config.configs;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseConfigTest {

    @Test
    public void saveAndReloadPreservesValues() throws Exception {
        Path file = Files.createTempFile("database", ".yml");
        DatabaseConfig config = new DatabaseConfig();
        config.setType("mysql");
        config.setAddress("localhost:3306");
        config.setName("testdb");
        config.setParams(List.of("autoReconnect=false", "useSSL=false"));
        config.setUseUserAndPassword(true);
        config.setUser("user");
        config.setPassword("pass");
        config.save(file);

        DatabaseConfig reloaded = new DatabaseConfig();
        reloaded.reload(file);

        assertEquals("mysql", reloaded.getType());
        assertEquals("localhost:3306", reloaded.getAddress());
        assertEquals("testdb", reloaded.getName());
        assertEquals(List.of("autoReconnect=false", "useSSL=false"), reloaded.getParams());
        assertEquals(true, reloaded.isUseUserAndPassword());
        assertEquals("user", reloaded.getUser());
        assertEquals("pass", reloaded.getPassword());
    }
}
