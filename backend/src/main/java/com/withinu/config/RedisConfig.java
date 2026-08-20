package com.withinu.config;

import io.lettuce.core.RedisURI;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisProperties properties) {
        if (properties.getUrl() != null && !properties.getUrl().isBlank()) {
            RedisURI uri = RedisURI.create(properties.getUrl());
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(uri.getHost());
            config.setPort(uri.getPort());
            if (uri.getUsername() != null) {
                config.setUsername(uri.getUsername());
            }
            if (uri.getPassword() != null) {
                config.setPassword(RedisPassword.of(new String(uri.getPassword())));
            }
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder();
            if (uri.isSsl()) {
                builder.useSsl();
            }
            return new LettuceConnectionFactory(config, builder.build());
        }
        return new LettuceConnectionFactory(
            new RedisStandaloneConfiguration(properties.getHost(), properties.getPort()));
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public DefaultRedisScript<Long> incrementAndExpireScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
              redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """);
        script.setResultType(Long.class);
        return script;
    }
}