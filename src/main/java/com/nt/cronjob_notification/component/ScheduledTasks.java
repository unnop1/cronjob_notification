package com.nt.cronjob_notification.component;

import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.RowFilter.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.cronjob_notification.entity.SaMetricNotificationEntity;
import com.nt.cronjob_notification.service.ScheduleNotificationService;

@Component
public class ScheduledTasks {

    private HashMap<String,Integer> cacheDBOMCountNotification = new HashMap<String,Integer>();

    private HashMap<String,Integer> cacheOMCountNotification = new HashMap<String,Integer>();

    private HashMap<String,Integer> cacheTriggerCountNotification = new HashMap<String,Integer>();

    private HashMap<String,SaMetricNotificationEntity> stackDBOMSendNotification = new HashMap<String,SaMetricNotificationEntity>();

    private HashMap<String,SaMetricNotificationEntity> stackOMSendNotification = new HashMap<String,SaMetricNotificationEntity>();

    private HashMap<String,SaMetricNotificationEntity> stackTriggerMessageSendNotification = new HashMap<String,SaMetricNotificationEntity>();

    @Autowired
    private ScheduleNotificationService scheduleNotificationService;

    // Check alert metric only message
    @Scheduled(cron = "0 */5 * * * *") // Cron expression for running every minute 0 */20 * * * *
    public void executeRabbitMQError() throws SQLException, IOException {
        scheduleNotificationService.CheckRabbitMQMetrics(cacheDBOMCountNotification, stackOMSendNotification);
    }

    // Check alert metric only message
    @Scheduled(cron = "0 */5 * * * *") // Cron expression for running every minute 0 */20 * * * *
    public void executeDatabaseOMError() throws SQLException, IOException {
        scheduleNotificationService.CheckDatabaseOMMetrics(cacheOMCountNotification, stackDBOMSendNotification);
    }

    // Check alert metric trigger message
    @Scheduled(cron = "0 */1 * * * *") // Cron expression for running every minute 0 */20 * * * *
    public void executeTriggerMessage() throws SQLException, IOException {
        scheduleNotificationService.CheckTriggerMessageMetrics(cacheTriggerCountNotification, stackTriggerMessageSendNotification);
    }

    // send notification metric only message
    @Scheduled(cron = "0 */20 0 * * *") // Cron expression for running every minute
    public void sendAlertDatabaseOMNotification() throws SQLException, IOException {
        for (String message : stackDBOMSendNotification.keySet()) {
            scheduleNotificationService.SendNotification("DbOmNotConnect", message, stackDBOMSendNotification.get(message));
        }
        stackDBOMSendNotification = new HashMap<String,SaMetricNotificationEntity>();
    }

    // send notification metric only message
    @Scheduled(cron = "0 */20 0 * * *") // Cron expression for running every minute
    public void sendAlertRabbitMQNotification() throws SQLException, IOException {
        for (String message : stackOMSendNotification.keySet()) {
            scheduleNotificationService.SendNotification("OmNotConnect", message, stackOMSendNotification.get(message));
        }
        stackOMSendNotification = new HashMap<String,SaMetricNotificationEntity>();
    }

    // send notification metric only message
    @Scheduled(cron = "0 */2 0 * * *") // Cron expression for running every minute
    public void sendAlertTriggerNotification() throws SQLException, IOException {
        for (String message : stackTriggerMessageSendNotification.keySet()) {
            scheduleNotificationService.SendNotification("CheckNumberOfTriggerInOrderTypeDatabase", message, stackTriggerMessageSendNotification.get(message));
        }
        stackTriggerMessageSendNotification = new HashMap<String,SaMetricNotificationEntity>();
    }

    // clear cache notification metric
    @Scheduled(cron = "0 0 0 * * *") // Cron expression for running every minute
    public void clearCache() throws SQLException, IOException {
        cacheDBOMCountNotification = new HashMap<String,Integer>();
        cacheOMCountNotification = new HashMap<String,Integer>();
        cacheTriggerCountNotification = new HashMap<String,Integer>();

    }
}