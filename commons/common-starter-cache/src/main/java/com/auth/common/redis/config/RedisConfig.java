package com.auth.common.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate 装配（String key + JSON value；会话索引另用纯字符串模板）
 *
 * @author Bunny
 */
@Slf4j
@Configuration
public class RedisConfig {

	private final ObjectMapper objectMapper;

	public RedisConfig(@Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * 创建RedisTemplate，使用String序列化key，JSON序列化value
	 */
	@Bean("redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(keySerializer());
		redisTemplate.setHashKeySerializer(keySerializer());
		redisTemplate.setValueSerializer(jsonRedisSerializer());
		redisTemplate.setHashValueSerializer(jsonRedisSerializer());
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	/**
	 * 会话索引专用：Hash / Set / ZSet 与 Lua ARGV 均使用纯字符串，避免 JSON 序列化污染 field 名
	 */
	@Bean("sessionRedisTemplate")
	public RedisTemplate<String, String> sessionRedisTemplate(LettuceConnectionFactory connectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		RedisSerializer<String> stringSerializer = keySerializer();
		redisTemplate.setKeySerializer(stringSerializer);
		redisTemplate.setValueSerializer(stringSerializer);
		redisTemplate.setHashKeySerializer(stringSerializer);
		redisTemplate.setHashValueSerializer(stringSerializer);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	private RedisSerializer<String> keySerializer() {
		return new StringRedisSerializer();
	}

	public RedisSerializer<Object> jsonRedisSerializer() {
		return new GenericJackson2JsonRedisSerializer(objectMapper);
	}

}