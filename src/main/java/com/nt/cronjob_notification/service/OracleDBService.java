package com.nt.cronjob_notification.service;

import java.sql.SQLException;
import java.sql.Connection;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class OracleDBService {

    @Value("${spring.datasource.url}")
    private String connectionUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private static HikariDataSource dataSource;

    private static final int CONNECTION_TIMEOUT_MS = 30000; // Timeout for checking if the connection is still valid
    private static final int MAX_RETRY_ATTEMPTS = 3; // Maximum number of retry attempts
    private static final long RETRY_DELAY_MS = 1000; // Delay between retry attempts
    

    public static synchronized Boolean getConnection() throws SQLException {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                Connection connection = dataSource.getConnection();
                if (isConnectionValid(connection)) {
                    return true;
                } else {
                    connection.close();
                    return true;
                }
            } catch (SQLException e) {
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new SQLException("Failed to obtain connection after maximum retry attempts");
    }

    private static boolean isConnectionValid(Connection connection) {
        try {
            return connection.isValid(CONNECTION_TIMEOUT_MS);
        } catch (SQLException e) {
            return false;
        }
    }

    public OracleDBService(@Value("${spring.datasource.url}") String jdbcUrl,
                               @Value("${spring.datasource.username}") String jdbcUsername,
                               @Value("${spring.datasource.password}") String jdbcPassword) {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);

        // Additional HikariCP configurations
        config.setMaximumPoolSize(10); // Maximum number of connections in the pool
        config.setMinimumIdle(5); // Minimum number of idle connections in the pool
        config.setConnectionTimeout(30000); // Timeout for establishing a new connection
        config.setIdleTimeout(600000); // Maximum time that a connection is allowed to remain idle
        config.setMaxLifetime(1800000); // Maximum lifetime of a connection in the pool

        // Add a connection test query to check if the connection is still valid
        config.setConnectionTestQuery("SELECT 1 FROM dual");


        dataSource = new HikariDataSource(config);
    }
}
