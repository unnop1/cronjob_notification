package com.nt.cronjob_notification.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.cronjob_notification.client.DistributeClient;
import com.nt.cronjob_notification.model.distribute.manage.metric.MetricsResp;
import com.nt.cronjob_notification.model.distribute.manage.metric.SaMetricNotificationData;
import com.nt.cronjob_notification.model.distribute.notification.AddNotification;
import com.nt.cronjob_notification.model.distribute.trigger.OrderTypeTriggerData;
import com.nt.cronjob_notification.model.distribute.trigger.TriggerOrderTypeCountResp;


@Service
public class DistributeService {

    @Value("${spring.distribute.host}")
    private String host;

    @Value("${spring.distribute.port}")
    private String port;
    
    @Value("${spring.distribute.username}")
    protected String username;

    @Value("${spring.distribute.password}")
    protected String password;

    @Value("${spring.distribute.device}")
    private String device;

    @Value("${spring.distribute.system}")
    private String system;

    @Value("${spring.distribute.browser}")
    private String browser;

    private DistributeClient client;

    private String accessToken = "";

    private void Login(){
        if (accessToken.isEmpty()){
            client = new DistributeClient(host, port, device, system, browser);
            accessToken = client.Login(username, password);
        }
    }

    public void Logout(){
        accessToken = "";
    }

    public MetricsResp ListAllMetrics(){
        System.out.println("username:"+username);
        System.out.println("password:"+password);
        System.out.println("host:"+host);
        System.out.println("port:"+port);
        Login();

        MetricsResp resp = client.ListMetrics();
        return resp;
    } 


    public TriggerOrderTypeCountResp mapOrderTypeTriggers(){
        System.out.println("username:"+username);
        System.out.println("password:"+password);
        System.out.println("host:"+host);
        System.out.println("port:"+port);
        Login();

        TriggerOrderTypeCountResp resp = client.OrderTypeTriggers();
        return resp;
    } 

    public void addNotificationMessage(AddNotification req){
        System.out.println("username:"+username);
        System.out.println("password:"+password);
        System.out.println("host:"+host);
        System.out.println("port:"+port);
        Login();

        client.AddNotificationMessage(req);
    } 
}