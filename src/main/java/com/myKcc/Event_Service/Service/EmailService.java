package com.myKcc.Event_Service.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void setMailSender(SimpleMailMessage email){

        mailSender.send(email);

    }

    @Async
    public void eventNotification(List<String> emails, String userMessage) {
        emails.forEach(email -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Event Notification");
                message.setText(userMessage);

                mailSender.send(message);
            } catch (MailException e) {
                System.out.println("Failed to send email to " + email + ": " + e.getMessage());
            }
        });
    }


    @Async
    public void eventNotifications(List<String> emails, String userMessage) {
        emails.forEach(email -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Event Notification");
                message.setText(userMessage);

                mailSender.send(message);

            } catch (MailException e) {
                System.out.println(e.getMessage());
            }
        });
    }



}
