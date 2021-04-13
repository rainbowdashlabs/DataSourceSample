package de.eldoria.databasesamples.config;

public class DbConfig {
    private DBSettings postgres;
    private DBSettings mariadb;

    public DBSettings getPostgres() {
        return postgres;
    }

    public DBSettings getMariadb() {
        return mariadb;
    }

    public static class DBSettings implements Cloneable {
        private String address;
        private String port;
        private String database;
        private String user;
        private String password;
        private int minConnections;
        private int maxConnections;

        public DBSettings(String address, String port, String database, String user, String password, int minConnections, int maxConnections) {
            this.address = address;
            this.port = port;
            this.database = database;
            this.user = user;
            this.password = password;
            this.minConnections = minConnections;
            this.maxConnections = maxConnections;
        }

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
            return Math.max(maxConnections, 1);
        }

        public String getUrl(DatabaseType databaseType) {
            return String.format("jdbs:%s://%s:%s/%s", databaseType.getDatabase(), address, port, database);
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setMinConnections(int minConnections) {
            this.minConnections = minConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }

        @Override
        public DBSettings clone() {
            return new DBSettings(address, port, database,user,password,minConnections,maxConnections);
        }
    }

}
