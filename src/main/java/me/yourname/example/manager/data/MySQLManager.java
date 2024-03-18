package me.yourname.example.manager.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.yourname.example.ExamplePlugin;
import me.yourname.example.utilities.Tasks;
import me.yourname.example.utilities.chat.Color;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLManager {

    private HikariDataSource hikariDataSource;
    private final String host = ExamplePlugin.getInstance().getConfigFile().getString("mysql.host");
    private final String username = ExamplePlugin.getInstance().getConfigFile().getString("mysql.username");
    private final String password = ExamplePlugin.getInstance().getConfigFile().getString("mysql.password");
    private final String database = ExamplePlugin.getInstance().getConfigFile().getString("mysql.database");
    private final String port = ExamplePlugin.getInstance().getConfigFile().getString("mysql.port");

    public MySQLManager() {
        Color.log("&eEnabling MySQL support!");
        Exception ex = connect();
        if (ex != null) {
            Color.log("&cThere was an error connecting to your database. Here's the suspect: &e" + ex.getLocalizedMessage());
            ex.printStackTrace();
            Bukkit.shutdown();
        } else {
            Color.log("&aManaged to successfully connect to: &e" + database + "&a!");
        }
        createT();
    }

    public void createT() {
        Tasks.runAsync(this::createTables);
    }

    private Exception connect() {
        try {
            HikariConfig config = new HikariConfig();
            Class.forName("org.mariadb.jdbc.Driver");
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            config.setJdbcUrl("jdbc:mariadb://" + host + ':' + port + '/' + database);
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            hikariDataSource = new HikariDataSource(config);
        } catch (Exception exception) {
            hikariDataSource = null;
            exception.printStackTrace();
            return exception;
        }
        return null;
    }

    public void shutdown() {
        close();
    }

    public void createTables() {
        createTable("ExamplePlugin","uuid VARCHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(16), points BIGINT(50)");
    }

    public boolean isInitiated() {
        return hikariDataSource != null;
    }

    public void close() {
        this.hikariDataSource.close();
    }


    /**
     * @return A new database connecting, provided by the Hikari pool.
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    /**
     * Create a new table in the database.
     *
     * @param name The name of the table.
     * @param info The table info between the round VALUES() brackets.
     */
    public void createTable(String name, String info) {
        new Thread(() -> {
            try (Connection resource = getConnection(); PreparedStatement statement = resource.prepareStatement("CREATE TABLE IF NOT EXISTS " + name + "(" + info + ");")) {
                statement.execute();
            } catch (SQLException exception) {
                Color.log("An error occurred while creating database table " + name + ".");
                exception.printStackTrace();
            }
        }).start();
    }

    /**
     * Execute an update to the database.
     *
     * @param query  The statement to the database.
     * @param values The values to be inserted into the statement.
     */
    public void execute(String query, Object... values) {
        new Thread(() -> {
            try (Connection resource = getConnection(); PreparedStatement statement = resource.prepareStatement(query)) {
                for (int i = 0; i < values.length; i++)
                    statement.setObject((i + 1), values[i]);

                statement.execute();
            } catch (SQLException exception) {
                Color.log("An error occurred while executing an update on the database.");
                Color.log("MySQL#execute : " + query);
                exception.printStackTrace();
            }
        }).start();
    }

    /**
     * Execute a query to the database.
     *
     * @param query    The statement to the database.
     * @param callback The data callback (Async).
     * @param values   The values to be inserted into the statement.
     */
    public void select(String query, SelectCall callback, Object... values) {
        new Thread(() -> {
            try (Connection resource = getConnection(); PreparedStatement statement = resource.prepareStatement(query)) {
                for (int i = 0; i < values.length; i++)
                    statement.setObject((i + 1), values[i]);

                callback.call(statement.executeQuery());
            } catch (SQLException exception) {
                Color.log("An error occurred while executing a query on the database.");
                Color.log("MySQL#select : " + query);
                exception.printStackTrace();
            }
        }).start();
    }

    public void updatePlayerPoints(UUID uuid, long points) {
        execute("UPDATE ExamplePlugin SET points=? WHERE uuid=?", points, uuid.toString());
    }

    public void updatePlayerName(UUID uuid, String name) {
        execute("UPDATE ExamplePlugin SET name=? WHERE uuid=?", name, uuid.toString());
    }

    public void delete() {
        execute("DELETE FROM ExamplePlugin");
    }

    public void deletePlayer(UUID uuid) {
        execute("DELETE FROM ExamplePlugin WHERE uuid=?", uuid.toString());
    }
}
