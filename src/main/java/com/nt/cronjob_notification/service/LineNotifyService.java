package com.nt.cronjob_notification.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nt.cronjob_notification.client.LineNotifyClient;

@Service
public class LineNotifyService {

    public void sendNotification(String message, String accessToken) {
        try {
            LineNotifyClient client = new LineNotifyClient(accessToken);
            client.sendNotification(message);
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
