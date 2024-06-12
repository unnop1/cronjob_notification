package com.nt.cronjob_notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;

@Service
public class RabbitMqService {
    @Value("${spring.rabbitmq.addresses}")
    private String rabbitAddresses;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    @Value("${spring.rabbitmq.virtual-host}")
    private String rabbitVirtualHost;

    public boolean checkConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(rabbitUsername);
        factory.setPassword(rabbitPassword);
        factory.setVirtualHost(rabbitVirtualHost);

        try {
            Address[] addresses = Address.parseAddresses(rabbitAddresses);
            try (Connection connection = factory.newConnection(addresses);
                 Channel channel = connection.createChannel()) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("Failed to check connection to RabbitMQ: " + e.getMessage());
            return false;
        }
    }
}

