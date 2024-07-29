package com.nt.cronjob_notification.component;

import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.cronjob_notification.service.ScheduleNotificationService;

@Component
public class ScheduledTasks {

    private HashMap<String,Integer> cacheNotification = new HashMap<String,Integer>();

    @Autowired
    private ScheduleNotificationService scheduleNotificationService;

    @Scheduled(cron = "0 * * * * *") // Cron expression for running every minute 0 */20 * * * *
    public void execute() throws SQLException, IOException {
        // scheduleNotificationService.CheckMetrics();
        scheduleNotificationService.CheckMetrics(cacheNotification);
    }

    @Scheduled(cron = "0 0 0 * * *") // Cron expression for running every minute
    public void clearCache() throws SQLException, IOException {
        cacheNotification = new HashMap<String,Integer>();
    }
}