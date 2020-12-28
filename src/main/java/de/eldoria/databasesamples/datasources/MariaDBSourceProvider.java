package de.eldoria.databasesamples.datasources;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.eldoria.databasesamples.config.DatabaseType;
import de.eldoria.databasesamples.config.DbConfig;
import de.eldoria.databasesamples.util.DataSourceProvider;
import de.eldoria.databasesamples.util.DbUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * MariaDB driver.
 * <p>
 * Utilized HikariCP for connection pooling.
 */
public class MariaDBSourceProvider extends DataSourceProvider<HikariDataSource> {
    public MariaDBSourceProvider(DbConfig.DBSettings config) throws SQLException {
        super(config);
    }

    @Override
    protected HikariDataSource initSource() {
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", DatabaseType.MARIADB.getDriverClass());
        DbUtil.mapSettings(props, getConfig());
        HikariConfig config = new HikariConfig(props);

        config.setMaximumPoolSize(getConfig().getMaxConnections());
        config.setConnectionTimeout(5 * 60 * 1000);

        return new HikariDataSource(config);
    }

    @Override
    protected void close(HikariDataSource source) {
        source.close();
    }

    @Override
    protected boolean testConnection(DataSource source) throws SQLException {
        try (Connection conn = source.getConnection()) {
            return conn.isValid(5 * 1000);
        }
    }
}
