package com.nt.cronjob_notification.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import com.nt.cronjob_notification.entity.NotificationMsgEntity;
import com.nt.cronjob_notification.entity.OrderTypeEntity;
import com.nt.cronjob_notification.entity.SaMetricNotificationEntity;
import com.nt.cronjob_notification.entity.view.trigger.TriggerOrderTypeCount;
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
    private  NativeConnectionService nativeConnectionService;

    @Autowired 
    private  SMTPService smtpService;

    @Autowired
    private  LineNotifyService lineNotifyService;

    private List<SaMetricNotificationEntity> ListAllMetrics(){
        List<SaMetricNotificationEntity> metrics = distributeService.ListAllMetrics();
        return metrics;
    } 

    private boolean CheckConnectOMAndTopUpRabbitMQ() {
        return rabbitMqService.checkConnection();
    }

    private boolean CheckConnectOMDatabase() throws SQLException {
        return nativeConnectionService.checkNativeConnection();
    }

    private HashMap<String, Integer> GetMapOrderTypes(){
        List<TriggerOrderTypeCount> triggerCount = distributeService.mapOrderTypeTriggers();
        HashMap<String, Integer> mapOrderTypeTrgHashMap = new HashMap<String, Integer>();
        for(TriggerOrderTypeCount trigger : triggerCount){
            System.out.println("ordertype: " + trigger.getORDERTYPE_NAME());
            System.out.println("TotalTrigger: " + trigger.getTotalTrigger());
            mapOrderTypeTrgHashMap.put(trigger.getORDERTYPE_NAME(), trigger.getTotalTrigger());
        }

        return mapOrderTypeTrgHashMap;
    }

    private String CheckNumberOfTriggerInOrderTypeDatabase(String triggerNotificationJSON, HashMap<String, Integer> mapOrderTypeTriggerSend) {
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
            if(metricMessages.size() > 0){
                notificationMessage = String.join(" , ", metricMessages);
            }
            System.out.println("notificationMessage:"+notificationMessage);
        }catch(Exception e){
            return notificationMessage;
        }
        return notificationMessage;
        
    }

    private void SendNotification(String action,String messageNotification, SaMetricNotificationEntity metricConfig){
        NotificationMsgEntity notification = new NotificationMsgEntity();
        notification.setAction(action);
        notification.setEmail(metricConfig.getEmail());
        notification.setMessage(messageNotification);
        ExecutorService executorService = Executors.newFixedThreadPool(3); // You can adjust the number of threads as needed

        try{
            // Send email
            executorService.submit(() -> smtpService.SendNotification(messageNotification, metricConfig.getEmail()));

            // Send Line Notify
            Integer isLineNotifyActive = metricConfig.getLINE_IS_ACTIVE();
            if (isLineNotifyActive >= 1) {
                executorService.submit(() -> lineNotifyService.SendNotification(messageNotification, metricConfig.getLINE_TOKEN()));
            }
        }catch (Exception e) {
            notification.setAction("Fail");
        }

        // Save notification message
        executorService.submit(() -> distributeService.addNotificationMessage(notification));

        // Shutdown the executor service when all tasks are complete
        executorService.shutdown();


    }

    public void test(){
        // List<SaMetricNotificationData> lists =ListAllMetrics();
        // SaMetricNotificationData tmp = lists.get(0);
        // System.out.println(tmp.getEmail());
        // System.out.println(tmp.getLineToken());
        // System.out.println(tmp.getTriggerNotiJson());
        // System.out.println(tmp.getUpdatedBy());
        // System.out.println(tmp.getOmNotConnect());
        // System.out.println(tmp.getTopupNotConnect());
        // System.out.println(tmp.getDbOmNotConnect());
    }

    public void CheckMetrics() throws SQLException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        Boolean isRabbitMQStatusOK = CheckConnectOMAndTopUpRabbitMQ();
        Boolean isOMDatabaseStatusOK = CheckConnectOMDatabase();
        HashMap<String, Integer> mapOrderTypeTriggerSend = GetMapOrderTypes();
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getOM_NOT_CONNECT().equals(1) && !isRabbitMQStatusOK){
                SendNotification("OmNotConnect","can not connect to rabbitmq",metric);
            }
            if (metric.getDB_OM_NOT_CONNECT().equals(1) && !isOMDatabaseStatusOK){
                SendNotification("DbOmNotConnect","can not connect to om database",metric);
            }
            String errorMessage = CheckNumberOfTriggerInOrderTypeDatabase(metric.getTRIGGER_NOTI_JSON(), mapOrderTypeTriggerSend);
            // String errorMessage = "more than setting to metric";
            if (!errorMessage.isEmpty()){
                SendNotification("CheckNumberOfTriggerInOrderTypeDatabase",errorMessage, metric);
            }
        }
    }
    
}
