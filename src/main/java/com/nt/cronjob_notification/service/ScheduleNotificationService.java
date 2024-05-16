package com.nt.cronjob_notification.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import com.nt.cronjob_notification.model.distribute.manage.metric.MetricsResp;
import com.nt.cronjob_notification.model.distribute.manage.metric.SaMetricNotificationData;
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

    private List<String> CheckNumberOfTriggerInOrderTypeDatabase(String triggerNotificationJSON) {
        HashMap<String, Integer> mapOrderTypeTriggerSend = new HashMap<String, Integer>();
        List<String> messageNotifications = new ArrayList<String>();
        JSONArray triggerConditions = new JSONArray(triggerNotificationJSON);
        for (int i = 0; i < triggerConditions.length(); i++) {
            JSONObject triggerCondition = triggerConditions.getJSONObject(i);
            // your code here
            Integer triggerCountConfig = triggerCondition.getInt("");
            String operatorConfig = triggerCondition.getString("");
            String OrderTypeNameConfig = triggerCondition.getString("");
            Integer value = mapOrderTypeTriggerSend.get(OrderTypeNameConfig);
            if (value != null && triggerCountConfig != null) {
                System.out.println("Value for key '" + OrderTypeNameConfig + "': " + value);
                if (Condition.doNumberOperation(operatorConfig, triggerCountConfig, value)){
                    String alertMessage = "Value for key '" + OrderTypeNameConfig + "': " + value;
                    messageNotifications.add(alertMessage);
                }
            }
        }
        return messageNotifications;
    }

    private void SendNotification(SaMetricNotificationData metricConfig){
        ExecutorService executorService = Executors.newFixedThreadPool(2); // You can adjust the number of threads as needed

        // Send email
        executorService.submit(() -> smtpService.SendNotification("message", metricConfig.getEmail()));

        // Send Line Notify
        Integer isLineNotifyActive = metricConfig.getLineIsActive();
        if (isLineNotifyActive >= 1) {
            executorService.submit(() -> lineNotifyService.sendNotification("message", metricConfig.getLineToken()));
        }

        // Shutdown the executor service when all tasks are complete
        executorService.shutdown();

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
        Boolean isRabbitMQStatusOK = CheckConnectOMAndTopUpRabbitMQ();
        Boolean isOMDatabaseStatusOK = CheckConnectOMDatabase();
        
        for (SaMetricNotificationData metric : metrics) {
            if (metric.getOmNotConnect().equals(1) && !isRabbitMQStatusOK){
                messageNotifications.add("can not connect to rabbitmq");
            }
            if (metric.getDbOmNotConnect().equals(1) && !isOMDatabaseStatusOK){
                messageNotifications.add("can not connect to om database");
            }
            concatenateArrays(messageNotifications, CheckNumberOfTriggerInOrderTypeDatabase(metric.getTriggerNotiJson()));
             
            if (messageNotifications.size() > 0){
                SendNotification(metric);
            }

            
        }
    }


    private List<String> concatenateArrays(List<String> arr1,
            List<String> arr2) {
        List<String> result = new ArrayList<>();
        System.arraycopy(arr1, 0, result, 0, arr1.size());
        System.arraycopy(arr2, 0, result, arr1.size(), arr2.size());
        return result;
    }
}
