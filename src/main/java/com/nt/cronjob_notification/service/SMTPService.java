package com.nt.cronjob_notification.service;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SMTPService {
    @Value("${smtp.host}")
    private String host = "ncmail.ntplc.co.th";

    @Value("${smtp.username}")
    private String username = "ommfe.alarm@nc.ntplc.co.th";

    @Value("${smtp.password}")
    private String password = "pqASCzDSUQKSLW81";

    @Value("${smtp.from}")
    private String from = "ommfe.alarm@nc.ntplc.co.th";

    public void SendNotification(String message, String toEmail) {
        // Email configurations
        String subject = "Notification Alert from Metric Cronjob";
        String body = message;

        // Set SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");  // Port for SSL
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "jakarta.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.debug", "true");
        properties.put("mail.smtp.ssl.trust", host); // Optional: trust the host

        // Create a session with authentication
        try {
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // Create a MimeMessage object
            MimeMessage messageText = new MimeMessage(session);

            // Set From: header field
            messageText.setFrom(new InternetAddress(from));

            // Set To: header field
            messageText.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            // Set Subject: header field
            messageText.setSubject(subject);

            // Set the actual message
            messageText.setText(body);

            // Send the message
            Transport.send(messageText);
            System.out.println("Email sent successfully!");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
