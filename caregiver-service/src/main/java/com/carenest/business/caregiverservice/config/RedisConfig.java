package com.carenest.business.caregiverservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.carenest.business.caregiverservice.application.dto.response.BulkCaregiverTop10Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.registerModule(new JavaTimeModule());
		return objectMapper;
	}

	@Bean
	@Primary
	public RedisSerializer<Object> redisSerializer() {
		return new GenericJackson2JsonRedisSerializer();
	}

	@Bean
	public RedisTemplate<String, BulkCaregiverTop10Response> bulkProductRedisTemplate(
		RedisConnectionFactory redisConnectionFactory
	) {
		RedisTemplate<String, BulkCaregiverTop10Response> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()));
		return template;
	}


}
