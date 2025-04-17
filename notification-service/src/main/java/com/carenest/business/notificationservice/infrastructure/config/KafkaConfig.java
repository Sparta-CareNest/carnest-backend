package com.carenest.business.notificationservice.infrastructure.config;

import com.carenest.business.common.event.payment.PaymentCancelledEvent;
import com.carenest.business.common.event.payment.PaymentCompletedEvent;
import com.carenest.business.common.event.reservation.ReservationCancelledEvent;
import com.carenest.business.common.event.reservation.ReservationStatusChangedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    private Map<String, Object> baseConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> targetType) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> configs = baseConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

        return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), deserializer);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> targetType) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(targetType));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> paymentCompletedKafkaListenerContainerFactory() {
        return factory(PaymentCompletedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCancelledEvent> paymentCancelledKafkaListenerContainerFactory() {
        return factory(PaymentCancelledEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReservationStatusChangedEvent> reservationStatusChangedKafkaListenerContainerFactory() {
        return factory(ReservationStatusChangedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReservationCancelledEvent> reservationCancelledKafkaListenerContainerFactory() {
        return factory(ReservationCancelledEvent.class);
    }

}
