package de.eldoria.util;

import com.google.gson.Gson;
import de.eldoria.databasesamples.config.DbConfig;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class TestUtil {
    private TestUtil() {
    }

    public static DbConfig loadDbConfig() throws IOException, ConfigurationException {
        TestUtil.class.getClassLoader().getResourceAsStream("config.json");

        String home = new File(".").getAbsoluteFile().getParentFile().toString();

        Path configPath = Paths.get(home, "config/config.json");

        if (!configPath.toFile().exists()) {
            Files.createDirectories(Paths.get(home, "config"));
            Files.createFile(configPath);
            try (InputStream resourceAsStream = TestUtil.class.getClassLoader().getResourceAsStream("config.json")) {
                assert resourceAsStream != null;
                Files.copy(
                        resourceAsStream,
                        configPath, StandardCopyOption.REPLACE_EXISTING);
            }
            throw new ConfigurationException("Please configure the config file");
        }

        return new Gson().fromJson(new InputStreamReader(Files.newInputStream(configPath)), DbConfig.class);

    }
}
