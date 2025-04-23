package com.carenest.business.reservationservice.infrastructure.config;

import com.carenest.business.common.kafka.KafkaRetryConfig;
import com.carenest.business.reservationservice.infrastructure.kafka.KafkaTopic;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(KafkaRetryConfig.class) // 공통 Kafka 재처리 설정 임포트
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

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
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}