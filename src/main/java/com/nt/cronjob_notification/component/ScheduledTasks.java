package com.nt.cronjob_notification.component;

import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.cronjob_notification.service.ScheduleNotificationService;

@Component
public class ScheduledTasks {

    @Autowired
    private ScheduleNotificationService scheduleNotificationService;

    @Scheduled(cron = "0 */30 * * * *") // Cron expression for running every minute
    public void execute() throws SQLException, IOException {
        // scheduleNotificationService.CheckMetrics();
        scheduleNotificationService.CheckMetrics();
    }
}