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
import com.nt.cronjob_notification.util.DateTime;
import com.nt.cronjob_notification.util.ServerJboss;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class ScheduleNotificationService {

    @Value("${env.name}")
    private String ENVNAME;

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
                        String alertMessage = "OrderType: " + OrderTypeNameConfig + " Total :" + value + "  "+ operatorConfig +" Metric value :" + triggerCountConfig;
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

    public boolean checkSendNotification(){
        
        String currentJbossIp = ServerJboss.getServerIP();
        // lineNotifyService.SendNotification("ip send: "+wlip, metricConfig.getLINE_TOKEN());
        if (currentJbossIp!=null) {
            if(!currentJbossIp.equals(wlip)){
                return false;
            }
        }

        LocalTime start = LocalTime.of(8, 0); // 08:00 AM
        LocalTime end = LocalTime.of(17, 0);  // 05:00 PM
        if(!DateTime.isCurrentTimeInRange(start, end)){
            return false;
        }

        return true; 
    }

    public void SendNotification(String action, String messageNotification, SaMetricNotificationEntity metricConfig) {
        List<String> emailList = Arrays.asList(metricConfig.getEmail().split(","));

        if (!checkSendNotification()) {
            return;
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

    public HashMap<String, HashMap<String, Object>> CheckTriggerMessageMetrics(HashMap<String, HashMap<String, Object>> cacheTriggerNotification) throws SQLException, IOException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        HashMap<String, Integer> mapOrderTypeTriggerSend = GetMapOrderTypes();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = df.format(new Date());
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getTRIGGER_IS_ACTIVE().equals(0)){
                continue;
            }

            // String triggerNotiJson = Convert.clobToString(metric.getTRIGGER_NOTI_JSON());
            List<String> errorMessages = CheckNumberOfTriggerInOrderTypeDatabase(metric.getTRIGGER_NOTI_JSON(), mapOrderTypeTriggerSend);
            // String errorMessage = "more than setting to metric";
            String keyPattern = String.join(",",errorMessages);

            // check cache for notification
            Integer maxCheckMetric = 1;
            HashMap<String, Object> cacheTrigger = cacheTriggerNotification.get(keyPattern);
            Integer cacheCount = 0;
            // String currentJbossIp = ServerJboss.getServerIP();
            if(cacheTrigger != null){
                cacheCount = Integer.valueOf(cacheTrigger.get("count").toString());
                if(cacheCount >= maxCheckMetric){
                    return cacheTriggerNotification;
                }
            }else{
                cacheTrigger = new HashMap<>();
                cacheTrigger.put("count", cacheCount);
            }

            if (errorMessages.size() > 0) {
                for (String errorMessage : errorMessages) {
                    String alertAction = "CheckNumberOfTriggerInOrderTypeDatabase";
                    String alertMsg = String.format("[%s] %s %s", ENVNAME, errorMessage, currentTime);

                    SendNotification(alertAction, alertMsg, metric);
                    
                    LogFlie.logMessage(
                        "ScheduleNotificationService", 
                        String.format("metric/%s/trigger_overload",LogFlie.dateFolderName()),
                        String.format(
                            "%s %s %s",
                            df.format(new Date()),
                            alertAction,
                            alertMsg
                        )
                    );
                }
                // save cache for notification
                cacheTrigger.put("message", keyPattern);
                cacheTrigger.put("metric", metric);
                cacheTrigger.put("time", currentTime);
                cacheTrigger.put("count", cacheCount+1);
                cacheTriggerNotification.put(keyPattern, cacheTrigger);
            }
        }
        return cacheTriggerNotification;
    }

    public HashMap<String, HashMap<String, Object>> CheckDatabaseOMMetrics(HashMap<String, HashMap<String, Object>> cacheOnlyMsgNotification) throws SQLException, IOException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        Boolean isOMDatabaseStatusOK = CheckConnectOMDatabase();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = df.format(new Date());
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getTRIGGER_IS_ACTIVE().equals(0)){
                continue;
            }

            // check cache for notification
            String metricId = String.valueOf(metric.getID());
            Integer maxCheckMetric = 1;
            HashMap<String, Object> cacheDB = cacheOnlyMsgNotification.get(metricId);
            Integer cacheCount = 0;
            // String currentJbossIp = ServerJboss.getServerIP();
            if(cacheDB != null){
                cacheCount = Integer.valueOf(cacheDB.get("count").toString());
                if(cacheCount >= maxCheckMetric){
                    return cacheOnlyMsgNotification;
                }
            }else{
                cacheDB = new HashMap<>();
                cacheDB.put("count", cacheCount);
            }

            if (metric.getDB_OM_NOT_CONNECT().equals(1) && !isOMDatabaseStatusOK){
                String alertAction = "DbOmNotConnect";
                String alertMessage = String.format("[%s] %s %s", ENVNAME, "can not connect to om database", currentTime);
                
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

                cacheDB.put("message", alertMessage);
                cacheDB.put("metric", metric);
                cacheDB.put("time", currentTime);
                cacheDB.put("count", cacheCount+1);
                cacheOnlyMsgNotification.put(metricId,cacheDB);
            }
        }
        return cacheOnlyMsgNotification;
    }

    public HashMap<String, HashMap<String, Object>> CheckRabbitMQMetrics(HashMap<String, HashMap<String, Object>> cacheOnlyMsgNotification) throws SQLException, IOException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        Boolean isRabbitMQStatusOK = CheckConnectOMAndTopUpRabbitMQ();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = df.format(new Date());
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getTRIGGER_IS_ACTIVE().equals(0)){
                continue;
            }

            // check cache for notification
            String metricId = String.valueOf(metric.getID()); 
            Integer maxCheckMetric = 1;
            HashMap<String, Object> cacheRabbitMQ = cacheOnlyMsgNotification.get(metricId);
            Integer cacheCount = 0;
            // String currentJbossIp = ServerJboss.getServerIP();
            if(cacheRabbitMQ != null){
                cacheCount = Integer.valueOf(cacheRabbitMQ.get("count").toString());
                if(cacheCount >= maxCheckMetric){
                    return cacheOnlyMsgNotification;
                }
            }else{
                cacheRabbitMQ = new HashMap<>();
                cacheRabbitMQ.put("count", cacheCount);
            }

            if (metric.getOM_NOT_CONNECT().equals(1) && !isRabbitMQStatusOK){
                String alertAction = "OmNotConnect";
                String alertMessage = String.format("[%s] %s %s", ENVNAME, "can not connect to rabbitmq", currentTime);

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

                cacheRabbitMQ.put("message", alertMessage);
                cacheRabbitMQ.put("metric", metric);
                cacheRabbitMQ.put("time", currentTime);
                cacheRabbitMQ.put("count", cacheCount+1);
                cacheOnlyMsgNotification.put(metricId, cacheRabbitMQ);
            }
        }

        return cacheOnlyMsgNotification;
    }
    
}
