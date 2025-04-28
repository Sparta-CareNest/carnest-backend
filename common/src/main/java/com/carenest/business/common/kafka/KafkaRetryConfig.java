package com.carenest.business.common.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@EnableKafka
public class KafkaRetryConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${spring.kafka.retry.initial-interval:1000}")
    private long initialInterval;

    @Value("${spring.kafka.retry.multiplier:2.0}")
    private double multiplier;

    @Value("${spring.kafka.retry.max-interval:10000}")
    private long maxInterval;

    @Value("${spring.kafka.dlq.suffix:.dlq}")
    private String deadLetterQueueSuffix;

    @Bean
    public KafkaTemplate<String, Object> retryableKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(maxRetryAttempts));
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "1000");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(maxRetryAttempts);
        expBackOff.setInitialInterval(initialInterval);
        expBackOff.setMultiplier(multiplier);
        expBackOff.setMaxInterval(maxInterval);

        // 데드레터 큐 발행자 설정
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (consumerRecord, exception) -> {
                    String originalTopic = consumerRecord.topic();
                    String deadLetterTopic = originalTopic + deadLetterQueueSuffix;
                    log.error("메시지 처리 실패 후 DLQ로 이동: {} -> {}, 키: {}, 예외: {}",
                            originalTopic, deadLetterTopic, consumerRecord.key(), exception.getMessage());
                    return deadLetterTopic;
                });

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, expBackOff);

        errorHandler.addNotRetryableExceptions(
                org.springframework.kafka.support.serializer.DeserializationException.class,
                org.apache.kafka.common.errors.RecordTooLargeException.class,
                java.lang.IllegalArgumentException.class
        );

        errorHandler.setLogLevel(org.springframework.kafka.listener.LoggingErrorHandler.Level.ERROR);

        return errorHandler;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.carenest.business.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            CommonErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    // DLQ 메시지 처리용 컨슈머 설정
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> deadLetterListenerContainerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.carenest.business.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap");

        ConsumerFactory<String, Object> dlqConsumerFactory = new DefaultKafkaConsumerFactory<>(props);

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dlqConsumerFactory);
        factory.setConcurrency(1);

        // DLQ는 재시도 없이 즉시 로깅만 수행
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(0L, 0L)); // 즉시 실패, 재시도 없음
        errorHandler.setLogLevel(org.springframework.kafka.listener.LoggingErrorHandler.Level.ERROR);
        factory.setCommonErrorHandler(errorHandler);

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public IdempotentMessageProcessor idempotentMessageProcessor() {
        return new IdempotentMessageProcessor();
    }

    public static class IdempotentMessageProcessor {
        private final Map<String, Boolean> processedMessages = new ConcurrentHashMap<>();

        public boolean processIfNotDuplicate(String messageKey) {
            return processedMessages.putIfAbsent(messageKey, Boolean.TRUE) == null;
        }

        public void markAsProcessed(String messageKey) {
            processedMessages.put(messageKey, Boolean.TRUE);
        }

        public void clearProcessedRecord(String messageKey) {
            processedMessages.remove(messageKey);
        }

        // 기존 처리 여부 확인
        public boolean isProcessed(String messageKey) {
            return processedMessages.containsKey(messageKey);
        }
    }
}