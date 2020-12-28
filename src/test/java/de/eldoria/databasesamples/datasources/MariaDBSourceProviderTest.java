package de.eldoria.databasesamples.datasources;

import de.eldoria.databasesamples.config.DbConfig;
import de.eldoria.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

class MariaDBSourceProviderTest {
    private static DbConfig config;

    @BeforeAll
    public static void loadConfig() throws IOException, ConfigurationException {
        config = TestUtil.loadDbConfig();
    }

    @Test
    public void testMariaDB() {
        Assertions.assertDoesNotThrow(() -> new MariaDBSourceProvider(config.getMariadb()));
    }
}
