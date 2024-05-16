package com.nt.cronjob_notification.service;

import java.util.Properties;
import javax.mail.*;
import org.springframework.stereotype.Service;
import javax.mail.internet.*;

@Service
public class SMTPService {
    public void SendNotification(String message, String toEmail){
        // Email configurations
        String host = "smtp.example.com";
        String username = "your_email@example.com";
        String password = "your_password";
        String from = "your_email@example.com";
        String to = toEmail;
        String subject = "Notification Alert from Metric Cronjob";
        String body = message;

        // Set SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");

        // Create a session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            // Create a MimeMessage object
            MimeMessage messageText = new MimeMessage(session);

            // Set From: header field
            messageText.setFrom(new InternetAddress(from));

            // Set To: header field
            messageText.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

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
