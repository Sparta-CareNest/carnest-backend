package com.carenest.business.reservationservice.infrastructure.config;

import com.carenest.business.common.kafka.KafkaRetryConfig;
import com.carenest.business.reservationservice.infrastructure.kafka.KafkaTopic;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Import(KafkaRetryConfig.class)
public class KafkaConfig {

    @Bean
    public NewTopic reservationCreatedTopic() {
        return TopicBuilder.name(KafkaTopic.RESERVATION_CREATED.getTopicName())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reservationCancelledTopic() {
        return TopicBuilder.name(KafkaTopic.RESERVATION_CANCELLED.getTopicName())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reservationStatusChangedTopic() {
        return TopicBuilder.name(KafkaTopic.RESERVATION_STATUS_CHANGED.getTopicName())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationEventTopic() {
        return TopicBuilder.name(KafkaTopic.NOTIFICATION_EVENT.getTopicName())
                .partitions(3)
                .replicas(1)
                .build();
    }

    // DLQ 토픽 정의
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
    public NewTopic reservationCreatedDLQTopic() {
        return TopicBuilder.name("reservation-created.dlq")
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
    public NewTopic reservationStatusChangedDLQTopic() {
        return TopicBuilder.name("reservation-status-changed.dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationDLQTopic() {
        return TopicBuilder.name("notification-event.dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }
}