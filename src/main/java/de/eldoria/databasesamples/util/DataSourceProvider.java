package de.eldoria.databasesamples.util;

import de.eldoria.databasesamples.config.DbConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceProvider<T extends DataSource> {
    private final T source;
    private final DbConfig.DBSettings config;

    public DataSourceProvider(DbConfig.DBSettings config) throws SQLException {
        this.config = config;
        this.source = initSource();
        if (testConnection(source)) {
            System.out.printf("Data Source %s successfully connected.", getClass().getSimpleName());
        } else {
            System.out.printf("Data source %s connection failed.", getClass().getSimpleName());
        }
    }

    protected abstract T initSource();

    /**
     * Close the data pool and all underlying connections.
     */
    public final void shutdown() {
        close(source);
    }

    /**
     * Get the stored data Source
     *
     * @return the stored data source
     */
    public DataSource getSource() {
        return source;
    }

    public DbConfig.DBSettings getConfig() {
        return config;
    }

    protected abstract void close(T source);

    protected boolean testConnection(DataSource source) throws SQLException {
        try (Connection conn = source.getConnection()) {
            return conn.isValid(5 * 1000);
        }
    }
}
