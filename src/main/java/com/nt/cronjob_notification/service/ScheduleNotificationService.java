package com.nt.cronjob_notification.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import com.nt.cronjob_notification.model.distribute.manage.metric.MetricsResp;
import com.nt.cronjob_notification.model.distribute.manage.metric.SaMetricNotificationData;
import com.nt.cronjob_notification.model.distribute.notification.AddNotification;
import com.nt.cronjob_notification.model.distribute.trigger.OrderTypeTriggerData;
import com.nt.cronjob_notification.util.Condition;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ScheduleNotificationService {

    @Autowired
    private  RabbitMqService rabbitMqService;

    @Autowired
    private  DistributeService distributeService;

    @Autowired 
    private  SMTPService smtpService;

    @Autowired
    private  LineNotifyService lineNotifyService;

    private List<SaMetricNotificationData> ListAllMetrics(){
        MetricsResp metrics = distributeService.ListAllMetrics();
        return metrics.getData();
    } 

    private boolean CheckConnectOMAndTopUpRabbitMQ() {
        return rabbitMqService.checkConnection();
    }

    private boolean CheckConnectOMDatabase() throws SQLException {
        return OracleDBService.getConnection();
    }

    private HashMap<String, Integer> GetMapOrderTypes(){
        List<OrderTypeTriggerData> triggerCount = distributeService.mapOrderTypeTriggers().getData();
        HashMap<String, Integer> mapOrderTypeTrgHashMap = new HashMap<String, Integer>();
        for(OrderTypeTriggerData trigger : triggerCount){
            System.out.println("ordertype: " + trigger.getOrdertype_NAME());
            System.out.println("TotalTrigger: " + trigger.getTotalTrigger());
            mapOrderTypeTrgHashMap.put(trigger.getOrdertype_NAME(), trigger.getTotalTrigger());
        }

        return mapOrderTypeTrgHashMap;
    }

    private String CheckNumberOfTriggerInOrderTypeDatabase(String triggerNotificationJSON) {
        HashMap<String, Integer> mapOrderTypeTriggerSend = GetMapOrderTypes();
        String notificationMessage = "";
        List<String> metricMessages = new ArrayList<String>();
        if (triggerNotificationJSON== null){
            return notificationMessage;
        }
        try{
            JSONArray triggerConditions = new JSONArray(triggerNotificationJSON);

            for (int i = 0; i < triggerConditions.length(); i++) {
                JSONObject triggerCondition = triggerConditions.getJSONObject(i);
                Integer triggerCountConfig = triggerCondition.getInt("value");
                String operatorConfig = triggerCondition.getString("operator");
                String OrderTypeNameConfig = triggerCondition.getString("order_type").toUpperCase();
                Integer value = mapOrderTypeTriggerSend.get(OrderTypeNameConfig);
                if (value != null && triggerCountConfig != null) {
                    System.out.println("Value for key '" + OrderTypeNameConfig + "': " + value);
                    if (Condition.doNumberOperation(operatorConfig, triggerCountConfig, value)){
                        String alertMessage = "OrderType: " + OrderTypeNameConfig + " , total :" + value + "  "+ operatorConfig +" metric value :" + triggerCountConfig;
                        metricMessages.add(alertMessage);
                    }
                }
            }
            notificationMessage = String.join(" , ", metricMessages);
        }catch(Exception e){
            return notificationMessage;
        }
        return notificationMessage;
        
    }

    private void SendNotification(List<String> messageNotifications, SaMetricNotificationData metricConfig){
        for (String message : messageNotifications){
            AddNotification notification = new AddNotification();
            notification.setAction("SendNotification");
            notification.setEmail(metricConfig.getEmail());
            notification.setMessage(message);
            ExecutorService executorService = Executors.newFixedThreadPool(3); // You can adjust the number of threads as needed

            // Send email
            executorService.submit(() -> smtpService.SendNotification(message, metricConfig.getEmail()));

            // Send Line Notify
            Integer isLineNotifyActive = metricConfig.getLineIsActive();
            if (isLineNotifyActive >= 1) {
                executorService.submit(() -> lineNotifyService.SendNotification(message, metricConfig.getLineToken()));
            }

            // Save notification message
            executorService.submit(() -> distributeService.addNotificationMessage(notification));

            // Shutdown the executor service when all tasks are complete
            executorService.shutdown();


        }

    }

    public void test(){
        List<SaMetricNotificationData> lists =ListAllMetrics();
        SaMetricNotificationData tmp = lists.get(0);
        System.out.println(tmp.getEmail());
        System.out.println(tmp.getLineToken());
        System.out.println(tmp.getTriggerNotiJson());
        System.out.println(tmp.getUpdatedBy());
        System.out.println(tmp.getOmNotConnect());
        System.out.println(tmp.getTopupNotConnect());
        System.out.println(tmp.getDbOmNotConnect());
    }

    public void CheckMetrics() throws SQLException{
        List<SaMetricNotificationData> metrics = ListAllMetrics();
        List<String> messageNotifications = new ArrayList<String>();
        // Boolean isRabbitMQStatusOK = CheckConnectOMAndTopUpRabbitMQ();
        // Boolean isOMDatabaseStatusOK = CheckConnectOMDatabase();
        
        for (SaMetricNotificationData metric : metrics) {
            // if (metric.getOmNotConnect().equals(1) && !isRabbitMQStatusOK){
            //     messageNotifications.add("can not connect to rabbitmq");
            // }
            // if (metric.getDbOmNotConnect().equals(1) && !isOMDatabaseStatusOK){
            //     messageNotifications.add("can not connect to om database");
            // }
            messageNotifications.add(CheckNumberOfTriggerInOrderTypeDatabase(metric.getTriggerNotiJson()));
            if (messageNotifications.size() > 0){
                SendNotification(messageNotifications, metric);
            }
        }

        
        distributeService.Logout();
    }
    
}
