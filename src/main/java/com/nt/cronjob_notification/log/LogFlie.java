package com.nt.cronjob_notification.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.springframework.boot.logging.java.SimpleFormatter;

public class LogFlie {

    public static String dateFolderName() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MMyyyy");
        return df.format(date);
    }

	public static void logMessage(String className, String path, String messageLog) {
        Logger logger = Logger.getLogger(className);

        try {
            Date date = new Date();
            
            // Use JBoss data directory
            String jbossDataDir = "./data/logs/cronjob_notification/";

            String pathLog = jbossDataDir + "/" + path + "/";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String fileName = dateFormat.format(date) + ".log";

            // Ensure directory exists, create if it doesn't
            File dir = new File(pathLog);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + pathLog);
                }
            }

            // Configure FileHandler for log rotation
            // Here, we set a file size limit of 1MB (1 * 1024 * 1024 bytes) and a maximum of 5 log files.
            FileHandler fileHandler = new FileHandler(pathLog + "/" + fileName, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Prevents logging to console

            // Log the message
            logger.info(messageLog);

            // Close the handler to ensure the log is written
            fileHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating directory or file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("General error: " + e.getMessage());
        }
    }

}