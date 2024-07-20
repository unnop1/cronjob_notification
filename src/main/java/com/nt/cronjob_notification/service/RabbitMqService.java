package com.nt.cronjob_notification.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;

@Service
public class RabbitMqService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqService.class);
    
    private final ConnectionFactory connectionFactory;

    @Autowired
    public RabbitMqService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Checks the connection to RabbitMQ.
     *
     * @return true if the connection is successful, false otherwise
     */
    public boolean checkConnection() {
        com.rabbitmq.client.Connection connection = null;
        try {
            connection = connectionFactory.createConnection().getDelegate();
            if (connection == null) {
                logger.error("Failed to create connection: connection is null");
                return false;
            }
            try (Channel channel = connection.createChannel()) {
                logger.info("Successfully connected to RabbitMQ");
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to check connection to RabbitMQ: " + e.getMessage(), e);
            return false;
        }
    }
}
