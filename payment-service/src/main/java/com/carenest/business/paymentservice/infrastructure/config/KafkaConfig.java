package com.carenest.business.paymentservice.infrastructure.config;

import com.carenest.business.common.kafka.KafkaRetryConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Import(KafkaRetryConfig.class)
public class KafkaConfig {

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name("payment-completed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCancelledTopic() {
        return TopicBuilder.name("payment-cancelled")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reservationCancelledTopic() {
        return TopicBuilder.name("reservation-cancelled")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic caregiverAcceptTopic() {
        return TopicBuilder.name("caregiver-accept")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationEventTopic() {
        return TopicBuilder.name("notification-event")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCompletedDLQTopic() {
        return TopicBuilder.name("payment-completed.dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCancelledDLQTopic() {
        return TopicBuilder.name("payment-cancelled.dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reservationCancelledDLQTopic() {
        return TopicBuilder.name("reservation-cancelled.dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic caregiverAcceptDLQTopic() {
        return TopicBuilder.name("caregiver-accept.dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationEventDLQTopic() {
        return TopicBuilder.name("notification-event.dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }
}