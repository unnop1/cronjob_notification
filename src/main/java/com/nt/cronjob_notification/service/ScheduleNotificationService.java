package com.nt.cronjob_notification.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import com.nt.cronjob_notification.entity.NotificationMsgEntity;
import com.nt.cronjob_notification.entity.SaMetricNotificationEntity;
import com.nt.cronjob_notification.entity.view.trigger.TriggerOrderTypeCount;
import com.nt.cronjob_notification.log.LogFlie;
import com.nt.cronjob_notification.util.Condition;
import com.nt.cronjob_notification.util.ServerJboss;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class ScheduleNotificationService {

    @Value("${notification.whitelist.ip}")
    private String wlip;

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

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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

    private List<String> CheckNumberOfTriggerInOrderTypeDatabase(String triggerNotificationJSON, HashMap<String, Integer> mapOrderTypeTriggerSend) {
        List<String> metricMessages = new ArrayList<String>();
        if (triggerNotificationJSON== null){
            return metricMessages;
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
                    if (Condition.doNumberOperation(operatorConfig, value, triggerCountConfig )){
                        String alertMessage = "OrderType: " + OrderTypeNameConfig + " , total :" + value + "  "+ operatorConfig +" metric value :" + triggerCountConfig;
                        metricMessages.add(alertMessage);
                    }
                }
            }
            // System.out.println("notificationMessage:"+notificationMessage);
        }catch(Exception e){
            metricMessages.add(e.getMessage());
        }
        return metricMessages;
        
    }

    public void SendNotification(String action, String messageNotification, SaMetricNotificationEntity metricConfig) {
        List<String> emailList = Arrays.asList(metricConfig.getEmail().split(","));

        String currentJbossIp = ServerJboss.getServerIP();
        // lineNotifyService.SendNotification("ip send: "+wlip, metricConfig.getLINE_TOKEN());
        if (currentJbossIp!=null) {
            if(!currentJbossIp.equals(wlip)){
                return;
            }
        }
        
        try {
            for (String email : emailList) {
                NotificationMsgEntity notification = new NotificationMsgEntity();
                notification.setAction(action);
                notification.setEmail(email);
                notification.setMessage(messageNotification);

                // Send email
                try {
                    smtpService.SendNotification(messageNotification, email);
                } catch (Exception e) {
                    notification.setAction("Fail");
                    e.printStackTrace();
                }

                

                // Save notification message
                try {
                    distributeService.addNotificationMessage(notification);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Send Line Notify
            Integer isLineNotifyActive = metricConfig.getLINE_IS_ACTIVE();
            if (isLineNotifyActive >= 1) {
                try {
                    lineNotifyService.SendNotification(messageNotification, metricConfig.getLINE_TOKEN());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Integer> CheckTriggerMessage5MMetrics(HashMap<String, Integer> cacheTriggerCountNotification) throws SQLException, IOException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        HashMap<String, Integer> mapOrderTypeTriggerSend = GetMapOrderTypes();
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getTRIGGER_IS_ACTIVE().equals(0)){
                continue;
            }

            // String triggerNotiJson = Convert.clobToString(metric.getTRIGGER_NOTI_JSON());
            List<String> errorMessages = CheckNumberOfTriggerInOrderTypeDatabase(metric.getTRIGGER_NOTI_JSON(), mapOrderTypeTriggerSend);
            // String errorMessage = "more than setting to metric";
            String keyPattern = String.join(",",errorMessages);

            // check cache for notification
            Integer maxCheckMetric = 3;
            Integer cacheCount = cacheTriggerCountNotification.get(keyPattern);
            // String currentJbossIp = ServerJboss.getServerIP();
            if(cacheCount != null){
                if(cacheCount >= maxCheckMetric){
                    return cacheTriggerCountNotification;
                }
            }else{
                cacheCount = 0;
            }

            if(cacheCount < 1){ 
                if (errorMessages.size() > 0) {
                    for (String errorMessage : errorMessages) {
                        String alertAction = "CheckNumberOfTriggerInOrderTypeDatabase";

                        SendNotification(alertAction, errorMessage, metric);
                        
                        LogFlie.logMessage(
                            "ScheduleNotificationService", 
                            String.format("metric/%s/trigger_overload",LogFlie.dateFolderName()),
                            String.format(
                                "%s %s %s",
                                df.format(new Date()),
                                alertAction,
                                errorMessage
                            )
                        );

                    }
                }
            }

            // save cache for notification
            cacheTriggerCountNotification.put(keyPattern, cacheCount+1);
        }
        return cacheTriggerCountNotification;
    }

    public HashMap<String, Integer> CheckTriggerMessage20MMetrics(HashMap<String, Integer> cacheTriggerCountNotification) throws SQLException, IOException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        HashMap<String, Integer> mapOrderTypeTriggerSend = GetMapOrderTypes();
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getTRIGGER_IS_ACTIVE().equals(0)){
                continue;
            }

            // String triggerNotiJson = Convert.clobToString(metric.getTRIGGER_NOTI_JSON());
            List<String> errorMessages = CheckNumberOfTriggerInOrderTypeDatabase(metric.getTRIGGER_NOTI_JSON(), mapOrderTypeTriggerSend);
            // String errorMessage = "more than setting to metric";
            String keyPattern = String.join(",",errorMessages);

            // check cache for notification
            Integer maxCheckMetric = 3;
            Integer cacheCount = cacheTriggerCountNotification.get(keyPattern);
            // String currentJbossIp = ServerJboss.getServerIP();
            if(cacheCount != null){
                if(cacheCount >= maxCheckMetric){
                    return cacheTriggerCountNotification;
                }
            }else{
                cacheCount = 0;
            }

            if(cacheCount <= maxCheckMetric){ 
                if (errorMessages.size() > 0) {
                    for (String errorMessage : errorMessages) {
                        String alertAction = "CheckNumberOfTriggerInOrderTypeDatabase";

                        SendNotification(alertAction, errorMessage, metric);
                        
                        LogFlie.logMessage(
                            "ScheduleNotificationService", 
                            String.format("metric/%s/trigger_overload",LogFlie.dateFolderName()),
                            String.format(
                                "%s %s %s",
                                df.format(new Date()),
                                alertAction,
                                errorMessage
                            )
                        );

                    }
                }
            }

            // save cache for notification
            cacheTriggerCountNotification.put(keyPattern, cacheCount+1);
        }
        return cacheTriggerCountNotification;
    }

    public void CheckDatabaseOMMetrics(HashMap<String, Integer> cacheOnlyMsgCountNotification, HashMap<String,SaMetricNotificationEntity> cacheOnlyMsgStack) throws SQLException, IOException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        Boolean isOMDatabaseStatusOK = CheckConnectOMDatabase();
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getTRIGGER_IS_ACTIVE().equals(0)){
                continue;
            }

            // check cache for notification
            String metricId = String.valueOf(metric.getID()); 
            Integer maxCheckMetric = 3;
            Integer cacheCount = cacheOnlyMsgCountNotification.get(metricId);
            // String currentJbossIp = ServerJboss.getServerIP();
            if(cacheCount != null){
                if(cacheCount >= maxCheckMetric){
                    return;
                }
            }else{
                cacheCount = 0;
                cacheOnlyMsgCountNotification.put(metricId, cacheCount);
            }

            if (metric.getDB_OM_NOT_CONNECT().equals(1) && !isOMDatabaseStatusOK){
                String alertAction = "DbOmNotConnect";
                String alertMessage = "can not connect to om database";
                
                if(cacheCount < 1){ 
                    SendNotification(alertAction,alertMessage,metric);
                    
                    LogFlie.logMessage(
                    "ScheduleNotificationService", 
                    String.format("metric/%s/connect",LogFlie.dateFolderName()),
                    String.format(
                        "%s %s %s",
                        df.format(new Date()),
                        alertAction,
                        alertMessage
                    )
                    );
                }else{
                    cacheOnlyMsgStack.put(alertMessage, metric);
                }

                cacheOnlyMsgCountNotification.put(alertMessage, cacheCount+1);
            }
        }
    }

    public void CheckRabbitMQMetrics(HashMap<String, Integer> cacheOnlyMsgCountNotification, HashMap<String,SaMetricNotificationEntity> cacheOnlyMsgStack) throws SQLException, IOException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        Boolean isRabbitMQStatusOK = CheckConnectOMAndTopUpRabbitMQ();
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getTRIGGER_IS_ACTIVE().equals(0)){
                continue;
            }

            // check cache for notification
            String metricId = String.valueOf(metric.getID()); 
            Integer maxCheckMetric = 3;
            Integer cacheCount = cacheOnlyMsgCountNotification.get(metricId);
            // String currentJbossIp = ServerJboss.getServerIP();
            if(cacheCount != null){
                if(cacheCount >= maxCheckMetric){
                    return;
                }
            }else{
                cacheCount = 0;
                cacheOnlyMsgCountNotification.put(metricId, cacheCount);
            }

            if (metric.getOM_NOT_CONNECT().equals(1) && !isRabbitMQStatusOK){
                String alertAction = "OmNotConnect";
                String alertMessage = "can not connect to rabbitmq";

                if(cacheCount < maxCheckMetric){                
                    SendNotification(alertAction,alertMessage,metric);
                
                    LogFlie.logMessage(
                    "ScheduleNotificationService", 
                    String.format("metric/%s/connect",LogFlie.dateFolderName()),
                    String.format(
                        "%s %s %s",
                        df.format(new Date()),
                        alertAction,
                        alertMessage
                    )
                    );
                }

                cacheOnlyMsgCountNotification.put(alertMessage, cacheCount+1);
            }

        }
    }
    
}
