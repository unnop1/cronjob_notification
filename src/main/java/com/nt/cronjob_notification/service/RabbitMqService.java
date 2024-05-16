package com.nt.cronjob_notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Service
public class RabbitMqService {
    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.port}")
    private int rabbitPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    public RabbitMqService(){
        
    }

    public boolean checkConnection(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setPort(rabbitPort);
        factory.setUsername(rabbitUsername);
        factory.setPassword(rabbitPassword);
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            return true;
        } catch (Exception e) {
            System.out.println("Failed to check connection to RabbitMQ: " + e.getMessage());
            return false;
        } 
    }
}
