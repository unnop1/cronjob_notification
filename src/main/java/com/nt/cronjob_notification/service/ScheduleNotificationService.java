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
                    if (Condition.doNumberOperation(operatorConfig, value, triggerCountConfig )){
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
            return notificationMessage+ " "+e.getMessage();
        }
        return notificationMessage;
        
    }

    private void SendNotification(String action, String messageNotification, SaMetricNotificationEntity metricConfig) {
        List<String> emailList = Arrays.asList(metricConfig.getEmail().split(","));

        String currentJbossIp = ServerJboss.getServerIP();
        lineNotifyService.SendNotification("ip send: "+currentJbossIp, metricConfig.getLINE_TOKEN());
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

    public void test() throws SQLException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        Boolean isRabbitMQStatusOK = CheckConnectOMAndTopUpRabbitMQ();
        Boolean isOMDatabaseStatusOK = CheckConnectOMDatabase();
        HashMap<String, Integer> mapOrderTypeTriggerSend = GetMapOrderTypes();
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getTRIGGER_IS_ACTIVE().equals(0)){
                continue;
            }
            if (metric.getOM_NOT_CONNECT().equals(1) && !isRabbitMQStatusOK){
                String alertAction = "OmNotConnect";
                String alertMessage = "can not connect to rabbitmq";

                // SendNotification(alertAction,alertMessage,metric);

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
            if (metric.getDB_OM_NOT_CONNECT().equals(1) && !isOMDatabaseStatusOK){
                String alertAction = "DbOmNotConnect";
                String alertMessage = "can not connect to om database";
                
                // SendNotification(alertAction,alertMessage,metric);
                
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

            // String triggerNotiJson = Convert.clobToString(metric.getTRIGGER_NOTI_JSON());
            String errorMessage = CheckNumberOfTriggerInOrderTypeDatabase(metric.getTRIGGER_NOTI_JSON(), mapOrderTypeTriggerSend);
            // String errorMessage = "more than setting to metric";
            if (!errorMessage.isEmpty()){
                String alertAction = "CheckNumberOfTriggerInOrderTypeDatabase";
                lineNotifyService.SendNotification(alertAction, metric.getLINE_TOKEN());
                
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
            String msgalert = String.format("check metric\n1. isRabbitMQStatusOK:%s\n2.isOMDatabaseStatusOK:%s\n3.CheckNumberOfTriggerInOrderTypeDatabaseError:%s",isRabbitMQStatusOK, isOMDatabaseStatusOK ,  errorMessage);
            lineNotifyService.SendNotification(msgalert, metric.getLINE_TOKEN());
            NotificationMsgEntity notification = new NotificationMsgEntity();
                notification.setAction("test action");
                notification.setEmail(metric.getEmail());
                notification.setMessage(msgalert);

            distributeService.addNotificationMessage(notification);
        }
        // smtpService.SendNotification("test", "arxzerocloud@gmail.com");
    }

    public void CheckMetrics(HashMap<String, Integer> cacheNotification) throws SQLException, IOException{
        List<SaMetricNotificationEntity> metrics = ListAllMetrics();
        Boolean isRabbitMQStatusOK = CheckConnectOMAndTopUpRabbitMQ();
        Boolean isOMDatabaseStatusOK = CheckConnectOMDatabase();
        HashMap<String, Integer> mapOrderTypeTriggerSend = GetMapOrderTypes();
        
        for (SaMetricNotificationEntity metric : metrics) {
            if (metric.getTRIGGER_IS_ACTIVE().equals(0)){
                continue;
            }

            // check cache for notification
            String metricId = String.valueOf(metric.getID()); 
            Integer maxCheckMetric = 3;
            Integer cacheCount = cacheNotification.get(metricId);
            if(cacheCount != null){
                if(cacheCount >= maxCheckMetric){
                    return;
                }
            }else{
                cacheCount = 0;
                cacheNotification.put(metricId, cacheCount);
            }

            if (metric.getOM_NOT_CONNECT().equals(1) && !isRabbitMQStatusOK){
                String alertAction = "OmNotConnect";
                String alertMessage = "can not connect to rabbitmq";

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
            if (metric.getDB_OM_NOT_CONNECT().equals(1) && !isOMDatabaseStatusOK){
                String alertAction = "DbOmNotConnect";
                String alertMessage = "can not connect to om database";
                
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

            // String triggerNotiJson = Convert.clobToString(metric.getTRIGGER_NOTI_JSON());
            String errorMessage = CheckNumberOfTriggerInOrderTypeDatabase(metric.getTRIGGER_NOTI_JSON(), mapOrderTypeTriggerSend);
            // String errorMessage = "more than setting to metric";
            if (!errorMessage.isEmpty()){
                String alertAction = "CheckNumberOfTriggerInOrderTypeDatabase";
                SendNotification(alertAction,errorMessage, metric);
                
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

            // save cache for notification
            cacheNotification.put(metricId, cacheCount+1);
        }
    }
    
}
