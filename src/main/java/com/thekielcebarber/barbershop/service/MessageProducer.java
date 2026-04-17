package com.thekielcebarber.barbershop.service;

import com.thekielcebarber.barbershop.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendAppointmentNotification(String message) {
    	rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
        System.out.println("📬 Mensaje enviado a la cola: " + message);
    }
}