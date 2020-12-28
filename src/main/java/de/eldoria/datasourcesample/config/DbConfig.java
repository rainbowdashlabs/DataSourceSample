package de.eldoria.datasourcesample.config;

public class DbConfig {
    public DBSettings postgres;
    public DBSettings mariadb;

    public DBSettings getPostgres() {
        return postgres;
    }

    public DBSettings getMariadb() {
        return mariadb;
    }

    public static class DBSettings {
        private String address;
        private String port;
        private String database;
        private String user;
        private String password;
        private final int minConnections = 1;
        private final int maxConnections = 10;

        public String getAddress() {
            return address;
        }

        public String getPort() {
            return port;
        }

        public String getDatabase() {
            return database;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public int getMinConnections() {
            return minConnections;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public String getUrl(DatabaseType databaseType) {
            return String.format("jdbs:%s://%s:%s/%s", databaseType.getDatabase(), address, port, database);
        }
    }

}
