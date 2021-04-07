package de.eldoria.databasesamples.config;

public enum DatabaseType {
    MARIADB("mysql", "org.mariadb.jdbc.MariaDbDataSource"),
    POSTGRESQL("postgresql", "org.postgresql.ds.PGSimpleDataSource");

    private final String database;
    private final String driverClass;

    DatabaseType(String database, String driverClass) {
        this.database = database;
        this.driverClass = driverClass;
    }

    public String getDatabase() {
        return database;
    }

    public String getDriverClass() {
        return driverClass;
    }
}
