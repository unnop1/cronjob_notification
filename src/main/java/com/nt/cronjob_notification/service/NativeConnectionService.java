package com.nt.cronjob_notification.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NativeConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(NativeConnectionService.class);

    @Value("${spring.datasource.jndi-name}")
    private String jndiName;

    private DataSource dataSource;

    @PostConstruct
    public void init() {
        try {
            Context ctx = new InitialContext();
            this.dataSource = (DataSource) ctx.lookup(jndiName);
            logger.info("JNDI DataSource initialized successfully: " + jndiName);
        } catch (NamingException e) {
            logger.error("Failed to lookup JNDI DataSource: " + e.getMessage(), e);
        }
    }

    /**
     * Checks the native connection.
     *
     * @return true if the native connection is valid, false otherwise
     */
    public Boolean checkNativeConnection() {
        if (dataSource == null) {
            logger.error("DataSource is not initialized");
            return false;
        }

        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            boolean isValid = stmt.execute("SELECT 1 FROM dual");
            logger.info("Native connection is valid: " + isValid);
            return isValid;
        } catch (SQLException e) {
            logger.error("SQLException occurred while checking native connection: " + e.getMessage(), e);
            return false;
        }
    }
}
