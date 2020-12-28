package de.eldoria.datasourcesample.samples;

import de.eldoria.datasourcesample.config.DbConfig;
import de.eldoria.datasourcesample.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

class PostgreSQLSourceTest {
    private static DbConfig config;

    @BeforeAll
    public static void loadConfig() throws IOException, ConfigurationException {
        config = TestUtil.loadDbConfig();
    }

    @Test
    public void testPostgreSQL() {
        Assertions.assertDoesNotThrow(() -> new PostgreSQLSource(config.getPostgres()));
    }
}