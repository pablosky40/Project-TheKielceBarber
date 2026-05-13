package com.thekielcebarber.barbershop.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Este es el nombre de la "tubería" por donde viajarán los avisos
    public static final String NOTIFICATION_QUEUE = "appointmentNotifications";

    @Bean
    public Queue notificationQueue() {
        // true = la cola sobrevive si reinicias el servidor de RabbitMQ
        return new Queue(NOTIFICATION_QUEUE, true);
    }
}