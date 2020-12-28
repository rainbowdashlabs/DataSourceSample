package de.eldoria.databasesamples.datarequests;

import de.eldoria.databasesamples.config.DbConfig;
import de.eldoria.databasesamples.datasources.MariaDBSourceProvider;
import de.eldoria.util.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.eldoria.util.TestUtil.clearDatabase;
import static de.eldoria.util.TestUtil.prepareDatabase;

public class DataRequestSample {

    private static DbConfig config;
    private static Logger logger;
    private static int benchmarkCalls = 25000;
    private static int benchmarkStringLength = 5000;

    @BeforeAll
    public static void loadConfig() throws IOException, ConfigurationException {
        config = TestUtil.loadDbConfig();
        logger = Logger.getAnonymousLogger();
    }

    @Test
    public void sampleDataRequests() {

        // In order to retrieve some data from a database we need a data source.
        // This data source can be retrieved by one of the pre implemented data sources we already have.
        // In this example we will use a MariaDB.

        // Lets start with creating the Data Source.
        DataSource source;
        try {
            MariaDBSourceProvider mariaDBSourceProvider = new MariaDBSourceProvider(config.getMariadb());
            source = mariaDBSourceProvider.getSource();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not build data source.", e);
            return;
        }

        // Ignore this.
        prepareDatabase(source, logger);

        // Now we have a data source. This source will allow us to use a connection pool.
        // It will also provide us usable connections and make them reusable instead of creating a new connection every time.

        // We will now retrieve a connection from our source and prepare a simple statement.
        // We will use prepared statements. this will protect us from SQL injection and malicious input.
        // We split the connection creation and the statement for better readability.
        // Since we declare the connection and statement inside the braces they will be closed when we exit the code block.
        // This will free the resources which were allocated for the result set and will also return the connection to the connection pool.

        try (Connection conn = source.getConnection(); PreparedStatement stmt =
                conn.prepareStatement("Insert into some_table(id, message) VALUES(?, ?)")) {
            // Lets insert the values into the query
            // We start with the first statement.
            stmt.setInt(1, 10);
            // And now we set the message
            stmt.setString(2, "some message");
            stmt.execute();
            logger.info("Inserted a new message");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not insert data.", e);
            return;
        }

        // Now that we have inserted data into our table, we may want to retrieve them as well.
        // You may see something like Select * from some_table.
        // However it is considered a best practise to explicit select colums you want to retrieve.
        try (Connection conn = source.getConnection(); PreparedStatement stmt =
                conn.prepareStatement("Select id, message from some_table where id = ?")) {
            // Lets use the same id we inserted previously.
            stmt.setInt(1, 10);
            // After executing the query we will get a result set.
            // The result set holds the returned rows.
            // We expect to only get one row since the id should be unique and should be only one time in the table.
            ResultSet resultSet = stmt.executeQuery();
            // To check if we have one row we need to request if the result set has a next column.
            // The result set is currently on no row and will jump on the first row when we check if a next row exists.
            if (resultSet.next()) {
                // If we enter this block we have some results.
                // now we select the column with the name message.
                // You can use the column index here instead, but using the column name makes it more readable
                String message = resultSet.getString("message");
                int id = resultSet.getInt("id");
                logger.info(String.format("Retrieved message id %d : %s", id, message));
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not retrieve data.", e);
            return;
        }

        // Lets insert another message.
        try (Connection conn = source.getConnection(); PreparedStatement stmt =
                conn.prepareStatement("Insert into some_table(id, message) VALUES(?, ?)")) {
            stmt.setInt(1, 11);
            stmt.setString(2, "some other message");
            stmt.execute();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not insert data.", e);
            return;
        }

        // Now we want to retrieve all messages
        // We do basically the same than before but without the where clause
        try (Connection conn = source.getConnection(); PreparedStatement stmt =
                conn.prepareStatement("Select id, message from some_table")) {
            ResultSet resultSet = stmt.executeQuery();
            // Instead of if we use while.
            // This will allow us to read all retrieved rows.
            while (resultSet.next()) {
                String message = resultSet.getString("message");
                int id = resultSet.getInt("id");
                logger.info(String.format("Retrieved message id %d : %s", id, message));
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not retrieve data.", e);
            return;
        }

        clearDatabase(source, logger);
    }
}
