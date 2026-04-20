package com.email_service.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.DefaultTyping;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Configuration
public class RedisConfig {

	private static final Duration TTL_24_HOURS = Duration.ofHours(24);

	@Bean
	RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		ObjectMapper objectMapper = JsonMapper.builder()
				.activateDefaultTyping(
						BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
						DefaultTyping.NON_FINAL,
						JsonTypeInfo.As.PROPERTY)
				.build();

		JacksonJsonRedisSerializer<Object> jsonSerializer = new JacksonJsonRedisSerializer<>(objectMapper,
				Object.class);

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(TTL_24_HOURS)
				.serializeKeysWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

		return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
	}
}
