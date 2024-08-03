package com.nt.cronjob_notification.component;

import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.swing.RowFilter.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.cronjob_notification.entity.SaMetricNotificationEntity;
import com.nt.cronjob_notification.service.ScheduleNotificationService;

@Component
public class ScheduledTasks {

    private Integer MaxCountPerDay = 3;

    private HashMap<String, HashMap<String, Object>> cacheTriggerNotification = new HashMap<String, HashMap<String, Object>>();

    private HashMap<String, HashMap<String, Object>> cacheRabbitMQNotification = new HashMap<String, HashMap<String, Object>>();

    HashMap<String, HashMap<String, Object>> cacheDatabaseNotification = new HashMap<String, HashMap<String, Object>>();


    @Autowired
    private ScheduleNotificationService scheduleNotificationService;

    // Check alert metric trigger message
    @Scheduled(cron = "0 */5 * * * *") // Cron expression for running every minute 0 */20 * * * *
    public void executeCheckAlertMessage() throws SQLException, IOException {
        cacheTriggerNotification = scheduleNotificationService.CheckTriggerMessageMetrics(cacheTriggerNotification);
        cacheRabbitMQNotification = scheduleNotificationService.CheckRabbitMQMetrics(cacheRabbitMQNotification);
        cacheDatabaseNotification = scheduleNotificationService.CheckDatabaseOMMetrics(cacheDatabaseNotification);
    }

    // Check alert metric trigger message
    @Scheduled(cron = "0 */20 * * * *") // Cron expression for running every minute 0 */20 * * * *
    public void executeStackSendMessage() throws SQLException, IOException {
        for (String key : cacheTriggerNotification.keySet()){
            HashMap<String,Object> cacheTrigger = cacheTriggerNotification.get(key);
            String[] messages = String.valueOf(cacheTrigger.get("message")).split(",");
            Integer count = Integer.valueOf(cacheTrigger.get("count").toString());
            String currentTime = cacheTrigger.get("time").toString();
            SaMetricNotificationEntity metric = (SaMetricNotificationEntity) cacheTrigger.get("metric");
            if (count > MaxCountPerDay){
                continue;
            }
            for (String message : messages){
                message = message + " at time " + currentTime;
                scheduleNotificationService.SendNotification("CheckNumberOfTriggerInOrderTypeDatabase", message, metric);
            }
        }

        for (String key : cacheRabbitMQNotification.keySet()){
            HashMap<String,Object> cacheRabbitMQ = cacheRabbitMQNotification.get(key);
            String message = String.valueOf(cacheRabbitMQ.get("message"));
            Integer count = Integer.valueOf(cacheRabbitMQ.get("count").toString());
            String currentTime = cacheRabbitMQ.get("time").toString();
            message = message + " at time " + currentTime;
            SaMetricNotificationEntity metric = (SaMetricNotificationEntity) cacheRabbitMQ.get("metric");
            if (count > MaxCountPerDay){
                continue;
            }
            scheduleNotificationService.SendNotification("CheckOMRabbitMQ", message, metric);
        }

        for (String key : cacheDatabaseNotification.keySet()){
            HashMap<String,Object> cacheOMDB = cacheDatabaseNotification.get(key);
            String message = String.valueOf(cacheOMDB.get("message"));
            Integer count = Integer.valueOf(cacheOMDB.get("count").toString());
            String currentTime = cacheOMDB.get("time").toString();
            message = message + " at time " + currentTime;
            SaMetricNotificationEntity metric = (SaMetricNotificationEntity) cacheOMDB.get("metric");
            if (count > MaxCountPerDay){
                continue;
            }
            scheduleNotificationService.SendNotification("CheckOMDatabase", message, metric);
        }
        // cacheTriggerCountNotification = scheduleNotificationService.CheckTriggerMessage20MMetrics(cacheTriggerCountNotification);
        // scheduleNotificationService.CheckRabbitMQMetrics(cacheDBOMCountNotification, stackOMSendNotification);
        // scheduleNotificationService.CheckDatabaseOMMetrics(cacheOMCountNotification, stackDBOMSendNotification);
    }

    // clear cache notification metric
    @Scheduled(cron = "0 0 0 * * *") // Cron expression for running every minute
    public void clearCache() throws SQLException, IOException {
        cacheDatabaseNotification = new HashMap<String, HashMap<String, Object>>();
        cacheTriggerNotification = new HashMap<String, HashMap<String, Object>>();
        cacheRabbitMQNotification = new HashMap<String, HashMap<String, Object>>();

    }
}