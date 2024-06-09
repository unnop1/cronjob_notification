package com.nt.cronjob_notification.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.stereotype.Service;

@Service
public class NativeConnectionService {

    @PersistenceContext
    private EntityManager entityManager;

    public Boolean checkNativeConnection() {
        Connection connection = null;
        try {
            connection = entityManager.unwrap(Connection.class);

            // Perform a simple query to check the connection
            try (Statement stmt = connection.createStatement()) {
                boolean isValid = stmt.execute("SELECT 1");
                System.out.println("Native connection is valid: " + isValid);
                return isValid;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
