package de.eldoria.databasesamples.datarequests;

import de.eldoria.databasesamples.config.DbConfig;
import de.eldoria.databasesamples.datasources.MariaDBSourceProvider;
import de.eldoria.util.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.eldoria.util.TestUtil.clearDatabase;
import static de.eldoria.util.TestUtil.prepareDatabase;

public class RequestBenchmark {
    private static DbConfig config;
    private static Logger logger;
    private static int benchmarkCalls = 50000;
    private static int benchmarkStringLength = 5000;

    @BeforeAll
    public static void loadConfig() throws IOException, ConfigurationException {
        config = TestUtil.loadDbConfig();
        logger = Logger.getAnonymousLogger();
    }

    @Test
    public void parallelRequests() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        DataSource source;
        try {
            MariaDBSourceProvider mariaDBSourceProvider = new MariaDBSourceProvider(config.getMariadb());
            source = mariaDBSourceProvider.getSource();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not build data source.", e);
            return;
        }


        prepareDatabase(source, logger);

        List<String> randomStrings = getRandomStrings(benchmarkCalls, benchmarkStringLength);

        long duration = System.currentTimeMillis();

        for (int i = 0; i < benchmarkCalls; i++) {
            int id = i;
            executor.execute(() -> {
                try (Connection conn = source.getConnection(); PreparedStatement stmt =
                        conn.prepareStatement("Insert into some_table(id, message) VALUES(?, ?)")) {
                    stmt.setInt(1, id);
                    stmt.setString(2, randomStrings.get(id));
                    stmt.execute();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Could not insert data.", e);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1L, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Timout", e);
        }

        duration = System.currentTimeMillis() - duration;

        logger.info(String.format("%d Parallel Requests took %d ms | %d seconds", benchmarkCalls, duration, duration / 1000));

        clearDatabase(source, logger);
    }

    @Test
    public void nonParallelRequests() {
        DataSource source;
        try {
            MariaDBSourceProvider mariaDBSourceProvider = new MariaDBSourceProvider(config.getMariadb());
            source = mariaDBSourceProvider.getSource();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not build data source.", e);
            return;
        }


        prepareDatabase(source, logger);

        List<String> randomStrings = getRandomStrings(benchmarkCalls, benchmarkStringLength);

        long duration = System.currentTimeMillis();

        for (int i = 0; i < benchmarkCalls; i++) {
            try (Connection conn = source.getConnection(); PreparedStatement stmt =
                    conn.prepareStatement("Insert into some_table(id, message) VALUES(?, ?)")) {
                stmt.setInt(1, i);
                stmt.setString(2, randomStrings.get(i));
                stmt.execute();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Could not insert data.", e);
            }
        }

        duration = System.currentTimeMillis() - duration;

        logger.info(String.format("%d Non Parallel Requests took %d ms | %d seconds", benchmarkCalls, duration, duration / 1000));

        clearDatabase(source, logger);
    }

    @Test
    public void singleConnectionParallelRequests() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        DbConfig.DBSettings clone = config.getMariadb().clone();
        clone.setMaxConnections(1);

        DataSource source;
        try {
            MariaDBSourceProvider mariaDBSourceProvider = new MariaDBSourceProvider(clone);
            source = mariaDBSourceProvider.getSource();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not build data source.", e);
            return;
        }

        prepareDatabase(source, logger);

        List<String> randomStrings = getRandomStrings(benchmarkCalls, benchmarkStringLength);

        long duration = System.currentTimeMillis();

        for (int i = 0; i < benchmarkCalls; i++) {
            int id = i;
            executor.execute(() -> {
                try (Connection conn = source.getConnection(); PreparedStatement stmt =
                        conn.prepareStatement("Insert into some_table(id, message) VALUES(?, ?)")) {
                    stmt.setInt(1, id);
                    stmt.setString(2, randomStrings.get(id));
                    stmt.execute();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Could not insert data.", e);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1L, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Timout", e);
        }

        duration = System.currentTimeMillis() - duration;

        logger.info(String.format("%d Single Connection Parallel Requests took %d ms | %d seconds", benchmarkCalls, duration, duration / 1000));

        clearDatabase(source, logger);
    }

    @Test
    public void singleConnectionNonParallelRequests() {
        DbConfig.DBSettings clone = config.getMariadb().clone();
        clone.setMaxConnections(1);
        DataSource source;
        try {
            MariaDBSourceProvider mariaDBSourceProvider = new MariaDBSourceProvider(clone);
            source = mariaDBSourceProvider.getSource();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not build data source.", e);
            return;
        }


        prepareDatabase(source, logger);

        List<String> randomStrings = getRandomStrings(benchmarkCalls, benchmarkStringLength);

        long duration = System.currentTimeMillis();

        for (int i = 0; i < benchmarkCalls; i++) {
            try (Connection conn = source.getConnection(); PreparedStatement stmt =
                    conn.prepareStatement("Insert into some_table(id, message) VALUES(?, ?)")) {
                stmt.setInt(1, i);
                stmt.setString(2, randomStrings.get(i));
                stmt.execute();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Could not insert data.", e);
            }
        }

        duration = System.currentTimeMillis() - duration;

        logger.info(String.format("%d Single Connection Non Parallel Requests took %d ms | %d seconds", benchmarkCalls, duration, duration / 1000));

        clearDatabase(source, logger);
    }


    private List<String> getRandomStrings(int amount, int length) {
        RandomString randomString = new RandomString(length, ThreadLocalRandom.current());
        return IntStream.range(0, amount).mapToObj(i -> randomString.nextString()).collect(Collectors.toList());
    }

    // Proudly stolen from https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
    public static class RandomString {

        /**
         * Generate a random string.
         */
        public String nextString() {
            for (int idx = 0; idx < buf.length; ++idx) {
                buf[idx] = symbols[random.nextInt(symbols.length)];
            }
            return new String(buf);
        }

        public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public static final String LOWER = UPPER.toLowerCase(Locale.ROOT);

        public static final String DIGITS = "0123456789";

        public static final String ALPHANUM = UPPER + LOWER + DIGITS;

        private final Random random;

        private final char[] symbols;

        private final char[] buf;

        public RandomString(int length, Random random, String symbols) {
            if (length < 1) throw new IllegalArgumentException();
            if (symbols.length() < 2) throw new IllegalArgumentException();
            this.random = Objects.requireNonNull(random);
            this.symbols = symbols.toCharArray();
            this.buf = new char[length];
        }

        /**
         * Create an alphanumeric string generator.
         */
        public RandomString(int length, Random random) {
            this(length, random, ALPHANUM);
        }

        /**
         * Create an alphanumeric strings from a secure generator.
         */
        public RandomString(int length) {
            this(length, new SecureRandom());
        }

        /**
         * Create session identifiers.
         */
        public RandomString() {
            this(21);
        }
    }

}
