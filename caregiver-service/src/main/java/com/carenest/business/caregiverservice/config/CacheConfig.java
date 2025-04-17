package com.carenest.business.caregiverservice.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {


	@Bean
	public RedisCacheManager cacheManager(
		RedisConnectionFactory connectionFactory,
		RedisSerializer<Object> redisSerializer
	) {
		RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration
			.defaultCacheConfig()
			.disableCachingNullValues()
			.entryTtl(Duration.ofSeconds(120))
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer)
			);


		return RedisCacheManager
			.builder(connectionFactory)
			.cacheDefaults(defaultCacheConfig)
			.build();
	}
}