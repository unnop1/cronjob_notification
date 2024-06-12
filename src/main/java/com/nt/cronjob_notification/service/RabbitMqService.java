package com.nt.cronjob_notification.service;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;

@Service
public class RabbitMqService {

    private final ConnectionFactory connectionFactory;

    @Autowired
    public RabbitMqService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public boolean checkConnection() {
        try (com.rabbitmq.client.Connection connection = connectionFactory.createConnection().getDelegate();
             Channel channel = connection.createChannel()) {
            System.out.println("Successfully connected to RabbitMQ");
            return true;
        } catch (Exception e) {
            System.out.println("Failed to check connection to RabbitMQ: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
