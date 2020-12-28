package de.eldoria.datasourcesample.samples;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.eldoria.datasourcesample.DataSourceProvider;
import de.eldoria.datasourcesample.DbUtil;
import de.eldoria.datasourcesample.config.DatabaseType;
import de.eldoria.datasourcesample.config.DbConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class PostgreSQLSource extends DataSourceProvider<HikariDataSource> {

    public PostgreSQLSource(DbConfig.DBSettings config) throws SQLException {
        super(config);
    }

    @Override
    public HikariDataSource initSource() {
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", DatabaseType.POSTGRESQL.getDriverClass());
        DbUtil.mapSettings(props, getConfig());
        HikariConfig config = new HikariConfig(props);

        config.setMaximumPoolSize(getConfig().getMaxConnections());
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
