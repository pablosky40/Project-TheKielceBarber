package com.thekielcebarber.barbershop.service;

import com.thekielcebarber.barbershop.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processNotification(String message) {
        try {
            System.out.println("RABBITMQ: Processing new message -> " + message);

            String[] parts = message.split("\\|");
            if (parts.length < 2) return;

            String toEmail = parts[0];
            String body = parts[1];

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(toEmail);
            email.setSubject("Booking Confirmation - The Kielce Barber");
            email.setText(body);

            mailSender.send(email);
            System.out.println("RABBITMQ: Email successfully sent to " + toEmail);

        } catch (Exception e) {
            System.err.println("RABBITMQ ERROR: Could not send email: " + e.getMessage());
        }
    }
}