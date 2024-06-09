package com.nt.cronjob_notification.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.cronjob_notification.client.DistributeClient;
import com.nt.cronjob_notification.entity.NotificationMsgEntity;
import com.nt.cronjob_notification.entity.SaMetricNotificationEntity;
import com.nt.cronjob_notification.entity.view.trigger.TriggerOrderTypeCount;
import com.nt.cronjob_notification.model.distribute.manage.metric.MetricsResp;
import com.nt.cronjob_notification.model.distribute.manage.metric.SaMetricNotificationData;
import com.nt.cronjob_notification.model.distribute.notification.AddNotification;
import com.nt.cronjob_notification.model.distribute.trigger.OrderTypeTriggerData;
import com.nt.cronjob_notification.model.distribute.trigger.TriggerOrderTypeCountResp;
import com.nt.cronjob_notification.repo.NotificationMessageRepo;
import com.nt.cronjob_notification.repo.OrderTypeRepo;
import com.nt.cronjob_notification.repo.SaMetricNotificationRepo;


@Service
public class DistributeService {

    @Autowired
    private OrderTypeRepo orderTypeRepo;

    @Autowired
    private NotificationMessageRepo notificationMessageRepo;

    @Autowired
    private SaMetricNotificationRepo saMetricNotificationRepo;

    public List<SaMetricNotificationEntity> ListAllMetrics(){
        List<SaMetricNotificationEntity> resp = saMetricNotificationRepo.ListSaMetrics();
        return resp;
    } 


    public List<TriggerOrderTypeCount> mapOrderTypeTriggers(){
        List<TriggerOrderTypeCount> resp = orderTypeRepo.AllOrderTypeTriggerCount();
        return resp;
    } 

    public void addNotificationMessage(NotificationMsgEntity req){
        notificationMessageRepo.save(req);
    } 
}