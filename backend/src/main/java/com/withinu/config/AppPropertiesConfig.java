package com.withinu.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WithinuProperties.class)
public class AppPropertiesConfig {
}