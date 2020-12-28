package de.eldoria.databasesamples.util;

import de.eldoria.databasesamples.config.DbConfig;

import java.sql.SQLException;
import java.util.Properties;

public final class DbUtil {
    private DbUtil() {
    }

    public static String prettyException(SQLException ex) {
        return "SQLException: " + ex.getMessage() + "\n"
                + "SQLState: " + ex.getSQLState() + "\n"
                + "VendorError: " + ex.getErrorCode();
    }

    public static void mapSettings(Properties props, DbConfig.DBSettings settings) {
        props.setProperty("dataSource.serverName", settings.getAddress());
        props.setProperty("dataSource.portNumber", settings.getPort());
        props.setProperty("dataSource.user", settings.getUser());
        props.setProperty("dataSource.password", settings.getPassword());
        props.setProperty("dataSource.databaseName", settings.getDatabase());
    }
}
